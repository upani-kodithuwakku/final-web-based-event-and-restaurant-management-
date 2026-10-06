/** Lombok boolean getters serialize isActive as active in the event API. */
export function normalizeEvent<T extends { active?: boolean; isActive?: boolean }>(row: T): T & { isActive: boolean } {
  return { ...row, isActive: row.isActive ?? row.active ?? false };
}
