import { getApiBaseUrl } from "@/config/config";
import { getUserFacingApiErrorMessage } from "@/utils/api-http-user-message";

const backendUrl = getApiBaseUrl() || "";
const baseUrl = `${backendUrl}/api/preplainte/pdf`;

export async function downloadPrePlaintePdf(payload: Record<string, any>) {
  const response = await fetch(baseUrl, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const bodyText = await response.text();
    throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
  }

  const contentDisposition = response.headers.get("Content-Disposition");

  let filename = "preplainte.pdf";
  if (contentDisposition) {
    const match = /filename="?([^"]+)"?/.exec(contentDisposition);
    if (match?.[1]) {
      filename = match[1];
    }
  }

  const blob = await response.blob();
  const url = globalThis.URL.createObjectURL(blob);

  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  globalThis.URL.revokeObjectURL(url);
}
