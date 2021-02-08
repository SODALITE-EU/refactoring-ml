# Gas Sensing Array Forecast with VAR model

import matplotlib.pyplot as plt
# Importing libraries
import pandas as pd

import timeseries.var_multi as var_multi

# Importing Dataset
df = pd.read_csv("../testResources/dataset_multi_time.csv")
ds = df.drop(['Time'], axis=1)
ds2 = ds.copy()

forecast, valid, rmses = var_multi.fit_forecast(ds)
print(rmses)
fig, axes = plt.subplots(nrows=int(len(ds.columns) / 2), ncols=2, dpi=100, figsize=(10, 10))

for i, (col, ax) in enumerate(zip(ds.columns, axes.flatten())):
    forecast[col].plot(color='#F4511E', legend=True, ax=ax).autoscale(axis=' x', tight=True)
    valid[col].plot(color='#3949AB', legend=True, ax=ax)

    ax.set_title('Sensor: ' + col + ' - Actual vs Forecast')
    ax.xaxis.set_ticks_position('none')
    ax.yaxis.set_ticks_position('none')

    ax.spines["top"].set_alpha(0)
    ax.tick_params(labelsize=6)

plt.tight_layout()
plt.savefig('actual_forecast.png')
plt.show()

print(var_multi.fit_forecast_next(ds2))
