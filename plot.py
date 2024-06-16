from collections import Counter

import matplotlib.pyplot as plt
from matplotlib import ticker
import numpy as np
import pandas as pd
from scipy.stats import linregress


def read_csv(path: str) -> pd.DataFrame | None:
    try:
        df = pd.read_csv(path)
    except FileNotFoundError:
        print(f"File {path} not found.")
        return None
    df = df[df[df.columns[0]] != -1]
    return df


n_values = ['0010', '0100', '1000']
p_values = ['250', '500', '750']

column_pairs = [('mind', 'minf'), ('mind', 'maxc'), ('minf', 'maxc')]

for n in n_values:
    for col1, col2 in column_pairs:
        fig, axes = plt.subplots(1, len(p_values), figsize=(5 * len(p_values), 5))

        for ax, p in zip(axes, p_values):
            filename = f'results/graph_n{n}_p{p}.csv'
            df = read_csv(filename)
            if df is None:
                continue

            points = df[[col1, col2]].values
            freq = Counter(map(tuple, points))
            sizes = [freq[tuple(point)] * 10 for point in points]

            ax.scatter(df[col1], df[col2], s=sizes)

            x = df[col1]
            y = df[col2]
            lo = int(min(*x, *y))
            hi = int(max(*x, *y))
            slope, intercept, r_value, _, _ = linregress(x, y)
            line_x = np.linspace(lo, hi, 100)
            line_y = slope * line_x + intercept

            ax.plot(line_x, line_y, color='red', linewidth=2, label=f'r = {r_value:.2f}')
            ax.legend(bbox_to_anchor=(1, 1), loc=1, borderaxespad=0)

            ax.set_xlabel(col1)
            ax.set_ylabel(col2)
            ax.set_title(f'p={p}')

            ax.set_xticks(np.arange(lo, hi + 1, step=1))
            ax.set_yticks(np.arange(lo, hi + 2, step=1))

        fig.suptitle(f'Scatter plots of {col1} vs {col2} for n={n}')
        plt.tight_layout(rect=[0, 0, 1, 0.96])
        plt.savefig(f'plots/scatter_{col1}_{col2}_n{n}.png')
        plt.close()

    medians = {'mind': [], 'minf': [], 'maxc': []}
    means = {'mind': [], 'minf': [], 'maxc': []}
    labels = []

    for p in p_values:
        filename = f'results/graph_n{n}_p{p}.csv'
        df = read_csv(filename)
        if df is None:
            continue

        for h in ['mind', 'minf', 'maxc']:
            medians[h].append(df[h].median())
            means[h].append(df[h].mean())
        labels.append(f'n={n}\np={p}')

    for name, values in {'Median': medians, 'Mean': means}.items():
        fig, ax = plt.subplots(figsize=(10, 6))
        bar_width = 0.25
        index = np.arange(len(p_values))
        ax.bar(index - bar_width, values['mind'], width=bar_width, label=f'{name} mind')
        ax.bar(index, values['minf'], width=bar_width, label=f'{name} minf')
        ax.bar(index + bar_width, values['maxc'], width=bar_width, label=f'{name} maxc')

        ax.set_xlabel('n-p combinations')
        ax.set_ylabel(f'{name} values')
        ax.set_title(f'{name} values of mind, minf, and maxc for n={n}')
        ax.set_xticks(index)
        ax.set_xticklabels(labels)
        ax.set_yscale('log')
        fmt = ticker.StrMethodFormatter("{x:g}")
        ax.yaxis.set_minor_formatter(fmt)
        ax.legend()

        plt.xticks(rotation=45, ha='right')
        plt.tight_layout()
        plt.savefig(f'plots/{name.lower()}_n{n}.png')
        plt.close()
