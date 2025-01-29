// src/components/HomeGrid.jsx
import GridItem from '@/components/GridItem';

const timeControls = [
  'Bullet (1+0)',
  'Blitz (3+0)',
  'Rapid (10+0)',
  'Classical (30+0)',
  'UltraBullet (15s)',
  'Blitz (5+0)',
  'Bullet (2+1)',
  'HyperBullet (30s)',
  'Marathon (Infinite)'
];

export default function HomeGrid() {
  return (
    <div className="grid grid-cols-3 gap-6">
      {timeControls.map((timeControl, index) => (
        <GridItem key={index} title={timeControl} />
      ))}
    </div>
  );
}