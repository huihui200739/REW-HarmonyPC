import { probe, memProbe, sendTouch } from 'librewjvm.so';
import { zlib } from '@kit.BasicServicesKit';
import { fileIo as fs } from '@kit.CoreFileKit';
import { common } from '@kit.AbilityKit';
import { BusinessError } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { image } from '@kit.ImageKit';

@Entry
@Component
struct Index {
  @State log: string = '就绪：启动后自动探测';
  @State busy: boolean = false;
  @State rewImage: string = '';
  @State rewPixels: image.PixelMap | undefined = undefined;
  @State showImage: boolean = true;  // autoShow

  aboutToAppear(): void {
    this.run();
  }

  private async ensureJars(ctx: common.UIAbilityContext): Promise<string> {
    const jarsDir = ctx.filesDir + '/jars';
    if (!fs.accessSync(jarsDir)) {
      fs.mkdirSync(jarsDir, true);
    }
    // 已解压则跳过（以主 jar 存在为准）
    // 后端 jar 每次都覆盖，保证代码更新即时生效
    const awtJar = await ctx.resourceManager.getRawFileContent('ohos-awt.jar');
    const af = fs.openSync(jarsDir + '/ohos-awt.jar', fs.OpenMode.CREATE | fs.OpenMode.READ_WRITE | fs.OpenMode.TRUNC);
    fs.writeSync(af.fd, awtJar.buffer as ArrayBuffer);
    fs.closeSync(af);
    if (fs.accessSync(jarsDir + '/RoomEQ_Wizard_obf.jar')) {
      return jarsDir;
    }
    const zipPath = ctx.filesDir + '/rew-jars.zip';
    const data = await ctx.resourceManager.getRawFileContent('rew-jars.zip');
    const f = fs.openSync(zipPath, fs.OpenMode.CREATE | fs.OpenMode.READ_WRITE | fs.OpenMode.TRUNC);
    fs.writeSync(f.fd, data.buffer as ArrayBuffer);
    fs.closeSync(f);
    await zlib.decompressFile(zipPath, jarsDir);
    return jarsDir;
  }

  private async ensureRuntime(ctx: common.UIAbilityContext): Promise<string> {
    const jreDir = ctx.filesDir + '/jre';
    const modulesPath = jreDir + '/lib/modules';
    if (fs.accessSync(modulesPath)) {
      return jreDir;
    }
    if (!fs.accessSync(jreDir)) {
      fs.mkdirSync(jreDir, true);
    }
    const zipPath = ctx.filesDir + '/jre-runtime.zip';
    const data = await ctx.resourceManager.getRawFileContent('jre-runtime.zip');
    const file = fs.openSync(zipPath, fs.OpenMode.CREATE | fs.OpenMode.READ_WRITE | fs.OpenMode.TRUNC);
    fs.writeSync(file.fd, data.buffer as ArrayBuffer);
    fs.closeSync(file);
    await zlib.decompressFile(zipPath, jreDir);
    // 布署字体与字体配置（OHOS 没有 fontconfig，需要 JDK 自带配置）
    const fontsDir = ctx.filesDir + '/fonts';
    if (!fs.accessSync(fontsDir)) {
      fs.mkdirSync(fontsDir, true);
    }
    for (const f of ['Arial.ttf', 'Arial Bold.ttf', 'Arial Italic.ttf', 'Arial Bold Italic.ttf']) {
      const buf = await ctx.resourceManager.getRawFileContent('fonts/' + f);
      const out = fs.openSync(fontsDir + '/' + f, fs.OpenMode.CREATE | fs.OpenMode.READ_WRITE | fs.OpenMode.TRUNC);
      fs.writeSync(out.fd, buf.buffer as ArrayBuffer);
      fs.closeSync(out);
    }
    const cfg = await ctx.resourceManager.getRawFileContent('fontconfig.properties.src');
    const cfgOut = fs.openSync(jreDir + '/lib/fontconfig.properties.src',
      fs.OpenMode.CREATE | fs.OpenMode.READ_WRITE | fs.OpenMode.TRUNC);
    fs.writeSync(cfgOut.fd, cfg.buffer as ArrayBuffer);
    fs.closeSync(cfgOut);
    return jreDir;
  }

  private emit(out: string): void {
    for (const line of out.split('\n')) {
      if (line.length > 0) {
        hilog.info(0x0000, 'REWJVM', '%{public}s', line);
      }
    }
  }

  async run(): Promise<void> {
    if (this.busy) return;
    this.busy = true;
    this.log = '解压 runtime 并启动 JVM…';
    try {
      const ctx = this.getUIContext().getHostContext() as common.UIAbilityContext;
      const jarsDir = await this.ensureJars(ctx);
      hilog.info(0x0000, 'REWJVM', 'jars=%{public}s', jarsDir);
      const jreDir = await this.ensureRuntime(ctx);
      hilog.info(0x0000, 'REWJVM', 'java.home=%{public}s', jreDir);
      const mem: string = memProbe();
      this.emit('--- exec-mem probe ---\n' + mem);
      hilog.info(0x0000, 'REWJVM', 'MEMPROBE %{public}s', mem.replace(/\n/g, ' | '));
      const out: string = probe(jreDir, ctx.filesDir);
      this.emit(out);
      this.log = out;
    } catch (err) {
      const be = err as BusinessError;
      hilog.error(0x0000, 'REWJVM', 'ERROR %{public}d %{public}s', be.code, be.message);
      this.log = 'ERROR: ' + be.code + ' ' + be.message;
      // probe 完成后把渲染结果读进来（autoShow）
      const ctx2 = this.getUIContext().getHostContext() as common.UIAbilityContext;
      if (ctx2) {
        await this.loadRewImage(ctx2);
      }
    } finally {
      this.busy = false;
    }
  }


  private async loadRewImage(ctx: common.UIAbilityContext): Promise<void> {
    try {
      const path = ctx.filesDir + '/rew-window.png';
      const file = fs.openSync(path, fs.OpenMode.READ_ONLY);
      const size = fs.statSync(path).size;
      const buf = new ArrayBuffer(size);
      fs.readSync(file.fd, buf);
      fs.closeSync(file);
      const source = image.createImageSource(buf);
      this.rewPixels = await source.createPixelMap();
      this.log = '界面图已加载: ' + size + ' bytes';
    } catch (err) {
      const be = err as BusinessError;
      this.log = '加载界面图失败: ' + be.code + ' ' + be.message;
    }
  }

  build() {
    Column({ space: 12 }) {
      Text('REW · JVM 探针').fontSize(22).fontWeight(FontWeight.Bold)
      Row({ space: 8 }) {
        Button(this.showImage ? '看日志' : '看界面')
          .onClick(() => {
            this.showImage = !this.showImage;
            if (this.showImage) {
              const ctx = this.getUIContext().getHostContext() as common.UIAbilityContext;
              if (ctx) {
                this.rewImage = ctx.filesDir + '/rew-window.png';
                this.loadRewImage(ctx);
              }
            }
          })
      }
      Button(this.busy ? '运行中…' : '重新探测')
        .enabled(!this.busy)
        .onClick(() => { this.run(); })
      if (this.showImage) {
        XComponent({ id: 'rewSurface', type: 'surface', libraryname: 'rewjvm' })
          .width('100%')
          .layoutWeight(1)
          .onLoad(() => { this.log = 'XComponent surface ready'; })
          .onTouch((e: TouchEvent) => {
            if (e.touches.length > 0) {
              const t = e.touches[0];
              let type = 2;
              if (e.type === TouchType.Down) {
                type = 0;
              } else if (e.type === TouchType.Up) {
                type = 1;
              }
              try {
                sendTouch(type, Math.round(t.x), Math.round(t.y));
              } catch (err) {
                this.log = 'sendTouch failed';
              }
            }
          })
      } else {
        Scroll() {
          Text(this.log).fontSize(13).fontFamily('monospace').width('100%')
        }.layoutWeight(1).width('100%')
      }
    }
    .padding(16)
    .width('100%')
    .height('100%')
    .alignItems(HorizontalAlign.Start)
  }
}