export const probe: (javaHome: string, workDir: string) => string;
export const memProbe: () => string;

export const sendTouch: (type: number, x: number, y: number) => void;
