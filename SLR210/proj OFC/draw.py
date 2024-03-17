# 画图
import matplotlib.pyplot as plt

figure_path = 'figures'
data_path = 'savedatas'

# 导入pickle数据
import pickle
import glob

pickle_files = glob.glob(f"{data_path}/*.pickle")
loaded_data = {}
file_count = 0
for file in pickle_files:
    # 提取文件名作为字典的键，或者您可以选择其他方式命名
    # key = file.split('\\')[-1]  # 在Windows上使用file.split('\\')[-1]
    key = file_count
    file_count += 1
    # 打开并加载pickle文件
    with open(file, 'rb') as f:
        loaded_data[key] = pickle.load(f)

# 计算平均值
averages = {}
for key, value in loaded_data.items():
    for param, result in value.items():
        if param not in averages:
            averages[param] = []
        averages[param].append(result)
for param, value in averages.items():
    averages[param] = sum(value) / len(value)

for alpha in ['0', '0.1', '1']:
    # tle为x轴，N为不同的线
    plt.figure()
    plot_data = {}
    for (N, f, a, tle), value in averages.items():
        if a == alpha:
            if N not in plot_data:
                plot_data[N] = {'tle': [], 'value': []}
            plot_data[N]['tle'].append(int(tle))
            plot_data[N]['value'].append(value)
    # 按tle由小到大排序
    for N, data in plot_data.items():
        # 使用zip将tle和value组合后一起排序，然后解压
        sorted_pairs = sorted(zip(data['tle'], data['value']))
        # 更新plot_data字典中的数据
        data['tle'], data['value'] = zip(*sorted_pairs)
    # 绘制图形
    for N, data in plot_data.items():
        plt.plot(data['tle'], data['value'], marker='o', label=f'N={N}')
    plt.xlabel('tle')
    plt.ylabel('time (ms)')
    plt.title('Time for the first process to decide, alpha=' + alpha)
    plt.legend()
    plt.grid(True)
    fig_name = figure_path + '/tle_vs_time_a=' + alpha + '.png'
    plt.savefig(fig_name)
