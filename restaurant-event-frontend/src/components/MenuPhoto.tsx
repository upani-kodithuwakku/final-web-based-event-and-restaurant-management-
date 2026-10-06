const dishes = [
  'burrata & heirloom tomatoes', 'chocolate indulgence', 'chocolate pudding', 'garden citrus cooler',
  'garlic bread', 'grilled chicken', 'grilled salmon bowl', 'mango juice',
  'the gather burger', 'tomato soup', 'vegetable rice', 'wood-fired margherita',
];

/** Crop one dish from our locally hosted photographic collection. */
export default function MenuPhoto({ name, imageUrl }: { name: string; imageUrl?: string }) {
  const index = dishes.indexOf(name.trim().toLowerCase());
  if (index < 0) return imageUrl
    ? <img src={imageUrl} alt={name} loading="lazy" onError={e => { e.currentTarget.style.display = 'none'; }} />
    : <div role="img" aria-label={`${name}: photo coming soon`} style={{ height: '100%', display: 'grid', placeItems: 'center', background: '#f4eee5', color: '#746453' }}>Photo coming soon</div>;
  return <svg role="img" aria-label={name} viewBox={`${index % 4} ${Math.floor(index / 4)} 1 1`}
    preserveAspectRatio="xMidYMid slice" style={{ display: 'block', position: 'absolute', inset: 0, width: '100%', height: '100%' }}>
    <title>{name}</title>
    <image href="/images/menu-food-atlas.png" width="4" height="3" preserveAspectRatio="none" />
  </svg>;
}
