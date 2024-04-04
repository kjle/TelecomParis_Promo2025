# plot
import matplotlib.pyplot as plt

figure_path = 'figures'
data_path = 'savedatas'

# load data from pickle
import pickle
import glob

pickle_files = glob.glob(f"{data_path}/*.pickle")
loaded_data = {}
file_count = 0
for file in pickle_files:
    key = file_count
    file_count += 1
    with open(file, 'rb') as f:
        loaded_data[key] = pickle.load(f)

# calculate averages
averages = {}
for key, value in loaded_data.items():
    for param, result in value.items():
        if param not in averages:
            averages[param] = []
        averages[param].append(result)
for param, value in averages.items():
    averages[param] = sum(value) / len(value)

for alpha in ['0', '0.1','0.5', '1']:
    plt.figure()
    plot_data = {}
    for (N, f, a, tle), value in averages.items():
        if a == alpha:
            if N not in plot_data:
                plot_data[N] = {'tle': [], 'value': []}
            plot_data[N]['tle'].append(int(tle))
            plot_data[N]['value'].append(value)
    # sort by tle
    for N, data in plot_data.items():
        sorted_pairs = sorted(zip(data['tle'], data['value']))
        data['tle'], data['value'] = zip(*sorted_pairs)
    # plot
    for N, data in plot_data.items():
        plt.plot(data['tle'], data['value'], marker='o', label=f'N={N}')
    plt.xlabel('tle')
    plt.ylim(0,300)
    plt.ylabel('time (ms)')
    plt.title('Time for the first process to decide, alpha=' + alpha)
    plt.legend()
    plt.grid(True)
    fig_name = figure_path + '/tle_vs_time_a=' + alpha + '.png'
    plt.savefig(fig_name)
