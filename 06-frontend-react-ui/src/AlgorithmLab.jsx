import { useMemo, useState } from "react";

const initialValues = [3, 8, 12, 17, 24, 31, 42, 56, 73];

function buildSteps(values, target) {
  const steps = [];
  let low = 0;
  let high = values.length - 1;

  while (low <= high) {
    const middle = Math.floor((low + high) / 2);
    const value = values[middle];
    steps.push({
      low,
      high,
      middle,
      message: value === target
        ? `Found ${target} at index ${middle}.`
        : value < target
          ? `${value} is too small, so discard the left half.`
          : `${value} is too large, so discard the right half.`,
    });
    if (value === target) break;
    if (value < target) low = middle + 1;
    else high = middle - 1;
  }

  if (steps.length === 0 || steps.at(-1).message.startsWith("Found") === false) {
    steps.push({ low, high, middle: -1, message: `${target} is not in the array.` });
  }
  return steps;
}

export default function AlgorithmLab() {
  const [target, setTarget] = useState("24");
  const [stepIndex, setStepIndex] = useState(0);
  const values = initialValues;
  const steps = useMemo(() => buildSteps(values, Number(target)), [target, values]);
  const step = steps[Math.min(stepIndex, steps.length - 1)];
  const finished = stepIndex >= steps.length - 1;

  function reset() {
    setStepIndex(0);
  }

  return (
    <section className="lab" aria-labelledby="algorithm-lab-title">
      <div className="lab-heading">
        <div>
          <p className="eyebrow">Interactive algorithm lab</p>
          <h2 id="algorithm-lab-title">Binary search, one decision at a time</h2>
        </div>
        <label>
          Target
          <input
            aria-label="Binary search target"
            inputMode="numeric"
            value={target}
            onChange={(event) => {
              setTarget(event.target.value);
              setStepIndex(0);
            }}
          />
        </label>
      </div>
      <p>
        The search space is sorted. Each step checks the middle and removes half
        of the remaining candidates.
      </p>
      <div className="array-visual" aria-label="Binary search array">
        {values.map((value, index) => {
          const active = index >= step.low && index <= step.high;
          const middle = index === step.middle;
          return (
            <div className={`array-cell${active ? " active" : ""}${middle ? " middle" : ""}`} key={value}>
              <span>{value}</span>
              <small>{index}</small>
            </div>
          );
        })}
      </div>
      <p role="status" aria-live="polite">
        Step {Math.min(stepIndex + 1, steps.length)} of {steps.length}: {step.message}
      </p>
      <div className="lab-actions">
        <button type="button" onClick={() => setStepIndex((current) => Math.min(current + 1, steps.length - 1))} disabled={finished}>
          Next step
        </button>
        <button type="button" className="secondary-button" onClick={reset}>Reset</button>
      </div>
      <details>
        <summary>Interview explanation</summary>
        <p>
          The invariant is: if the target exists, it is always inside the
          inclusive range <code>[low, high]</code>. Each comparison removes a
          half, giving O(log n) time and O(1) extra space.
        </p>
      </details>
    </section>
  );
}
