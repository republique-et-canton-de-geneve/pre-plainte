import type { RipolSelection } from "@/types/ripol.interface";

const normalize = (value: string): string =>
  value
    .normalize("NFD")
    .replaceAll(/\p{Diacritic}/gu, "")
    .toLocaleLowerCase("fr")
    .trim();

export const filterRipolSelections = (items: RipolSelection[], search?: string): RipolSelection[] => {
  const query = normalize(search ?? "");
  if (!query) {
    return items;
  }
  return items.filter(item => {
    const label = normalize(item.label ?? "");
    const code = normalize(item.code ?? "");
    return label.includes(query) || code.includes(query);
  });
};
