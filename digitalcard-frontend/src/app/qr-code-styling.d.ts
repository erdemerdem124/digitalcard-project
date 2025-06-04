// src/app/qr-code-styling.d.ts
declare module 'qr-code-styling' {
  interface Options {
    width?: number;
    height?: number;
    type?: string;
    data?: string;
    image?: string;
    dotsOptions?: {
      color?: string;
      type?: string;
    };
    backgroundOptions?: {
      color?: string;
    };
    imageOptions?: {
      hideBackgroundDots?: boolean;
      imageSize?: number;
      margin?: number;
      crossOrigin?: string;
    };
    cornersSquareOptions?: {
      color?: string;
      type?: string;
    };
    cornersDotOptions?: {
      color?: string;
      type?: string;
    };
    qrOptions?: {
      typeNumber?: number;
      mode?: string;
      errorCorrectionLevel?: string;
    };
  }

  class QRCodeStyling {
    constructor(options: Options);
    append(container: HTMLElement): void;
    download(options?: { name?: string; extension?: string }): void;
    clear(): void;
  }

  export default QRCodeStyling;
}
