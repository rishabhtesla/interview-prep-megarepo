import { useState } from "react";

const stages = [
  {
    id: "raw",
    label: "Raw partner geometry",
    description: "The incoming line has an extra point and no normalized status.",
    collection: {
      type: "FeatureCollection",
      features: [{
        type: "Feature",
        properties: { status: "raw" },
        geometry: { type: "LineString", coordinates: [[12, 72], [38, 42], [64, 58], [88, 30]] },
      }],
    },
  },
  {
    id: "normalized",
    label: "Normalized geometry",
    description: "The transform removes the noisy point and standardizes coordinates.",
    collection: {
      type: "FeatureCollection",
      features: [{
        type: "Feature",
        properties: { status: "normalized" },
        geometry: { type: "LineString", coordinates: [[12, 72], [50, 50], [88, 30]] },
      }],
    },
  },
  {
    id: "enriched",
    label: "Enriched result",
    description: "A business property is added without changing the route shape.",
    collection: {
      type: "FeatureCollection",
      features: [{
        type: "Feature",
        properties: { status: "enriched", incidentId: "incident-7" },
        geometry: { type: "LineString", coordinates: [[12, 72], [50, 50], [88, 30]] },
      }],
    },
  },
];

function linePath(coordinates) {
  return coordinates.map(([x, y], index) => `${index === 0 ? "M" : "L"} ${x} ${y}`).join(" ");
}

export default function GeoJsonMutationLab() {
  const [stageIndex, setStageIndex] = useState(0);
  const stage = stages[stageIndex];

  return (
    <section className="geojson-lab" aria-label="GeoJSON mutation visualizer">
      <div className="lab-heading">
        <div>
          <p className="eyebrow">GeoJSON mutation lab</p>
          <h2>{stage.label}</h2>
          <p>{stage.description}</p>
        </div>
        <p className="lab-state">Stage {stageIndex + 1} of {stages.length}</p>
      </div>
      <svg className="geojson-canvas" viewBox="0 0 100 100" role="img"
        aria-label={`${stage.label}: ${stage.collection.features.length} feature`}>
        <path className="geojson-grid" d="M 10 10 H 90 M 10 50 H 90 M 10 90 H 90 M 10 10 V 90 M 50 10 V 90 M 90 10 V 90" />
        {stage.collection.features.map((feature) => (
          <path key={feature.properties.status} className="geojson-feature"
            d={linePath(feature.geometry.coordinates)} />
        ))}
      </svg>
      <p className="lab-detail">
        Properties: <code>{JSON.stringify(stage.collection.features[0].properties)}</code>
      </p>
      <div className="lab-actions">
        <button type="button" onClick={() => setStageIndex((value) => value - 1)}
          disabled={stageIndex === 0}>Previous stage</button>
        <button type="button" onClick={() => setStageIndex((value) => value + 1)}
          disabled={stageIndex === stages.length - 1}>Next stage</button>
      </div>
    </section>
  );
}
