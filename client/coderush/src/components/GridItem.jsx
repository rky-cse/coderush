// src/components/GridItem.jsx
export default function GridItem({ title }) {
    return (
      <div className="p-6 bg-white rounded-lg shadow-md hover:shadow-lg transition">
        <h3 className="text-lg font-semibold text-gray-800">{title}</h3>
      </div>
    );
  }