interface Props {
  initials: string;
  color: string;
  size?: number;
}

export function Avatar({ initials, color, size = 48 }: Props) {
  return (
    <div
      style={{
        width: size, height: size, borderRadius: "50%",
        background: `${color}20`, border: `2px solid ${color}35`,
        display: "flex", alignItems: "center", justifyContent: "center",
        fontSize: size * 0.3, fontWeight: 700, color, flexShrink: 0, letterSpacing: "0.5px",
      }}
    >
      {initials}
    </div>
  );
}
