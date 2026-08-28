export const IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];
export const MAX_IMAGE_SIZE = 2 * 1024 * 1024;

export function validatePrice(value: string) {
  const amount = Number(value);
  if (!Number.isFinite(amount) || amount <= 0) return "价格必须大于 0";
  return /^\d+(\.\d{1,2})?$/.test(value) ? "" : "价格最多保留两位小数";
}

export function validateImageFile(file: File) {
  if (!IMAGE_TYPES.includes(file.type)) return "仅支持 JPEG、PNG 或 WebP 图片";
  if (file.size > MAX_IMAGE_SIZE) return "图片大小不能超过 2 MB";
  return "";
}

export function fileToDataUrl(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(new Error("图片读取失败"));
    reader.readAsDataURL(file);
  });
}
