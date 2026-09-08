"""Dark Book's dependency-free baseline trainer, always launched and owned by Java."""
import argparse
import collections
import hashlib
import json
import math
from pathlib import Path


def tokens(text: str):
    return [word.lower().strip(".,!?;:#@()[]{}") for word in text.split() if len(word) > 1]


def vectorize(text: str, dimension: int = 64):
    vector = [0.0] * dimension
    for token in tokens(text):
        digest = hashlib.sha256(token.encode("utf-8")).digest()
        for index in range(dimension):
            vector[index] += digest[index % len(digest)] / 127.5 - 1.0
    norm = math.sqrt(sum(value * value for value in vector))
    return [value / norm for value in vector] if norm else vector


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dataset", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    source = Path(args.dataset)
    if not source.exists():
        raise SystemExit("No existe el dataset. Ejecuta primero el scanner de demostración.")
    groups = collections.defaultdict(list)
    for line in source.read_text(encoding="utf-8").splitlines():
        if line.strip():
            row = json.loads(line)
            groups[row.get("category", "general")].append(vectorize(row.get("text", "")))
    centroids = {}
    for label, vectors in groups.items():
        centroids[label] = [sum(values) / len(values) for values in zip(*vectors)]
    model = {"name": "darkbook-centroid-v1", "version": "1.0", "dimension": 64,
             "examples": sum(map(len, groups.values())), "labels": sorted(groups), "centroids": centroids}
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(model, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Modelo guardado: {output} ({model['examples']} ejemplos)")


if __name__ == "__main__":
    main()

