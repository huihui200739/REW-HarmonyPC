import { probe, memProbe } from 'librewjvm.so';
import { zlib } from '@kit.BasicServicesKit';
import { fileIo as fs } from '@kit.CoreFileKit';
import { common } from '@kit.AbilityKit';
import { BusinessError } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';

@Entry
@Component
struct Index {
  @State log: string = '就绪：启动后自动探测';
  @State busy: boolean = false;

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
    } finally {
      this.busy = false;
    }
  }

  build() {
    Column({ space: 12 }) {
      Text('REW · JVM 探针').fontSize(22).fontWeight(FontWeight.Bold)
      Button(this.busy ? '运行中…' : '重新探测')
        .enabled(!this.busy)
        .onClick(() => { this.run(); })
      Scroll() {
        Text(this.log).fontSize(13).fontFamily('monospace').width('100%')
      }.layoutWeight(1).width('100%')
    }
    .padding(16)
    .width('100%')
    .height('100%')
    .alignItems(HorizontalAlign.Start)
  }
}