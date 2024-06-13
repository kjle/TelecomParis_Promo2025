import re
import os
import pandas as pd
import matplotlib.pyplot as plt

log_info = [
    "servers number",
    "Time for ftp sending splits",
    "Time for sending node info",
    "Time for SPLIT",
    "Time for PRESHUFFLE",
    "Time for WAITREADY",
    "Time for SHUFFLE",
    "Time for CALCULATE",
    "Time for RANGE",
    "Time for PRESHUFFLE2",
    "Time for WAITREADY2",
    "Time for SHUFFLE2",
    "Time for communication",
    "Time for computation",
    "Time for synchronization",
    "Time ratio",
    "Time total"
]

log_dir = "./summary"
log_files = [f for f in os.listdir(log_dir) if os.path.isfile(os.path.join(log_dir, f))]

all_logs_info = []

for log_file in log_files:
    with open(os.path.join(log_dir, log_file), 'r') as file:
        log_content = file.read()

    regex_patterns = {
        "default": r"\[INFO\]\[MyClient\]\[main\] (.+?): ([0-9\.]+) ms",
        "Time ratio": r"\[INFO\]\[MyClient\]\[main\] Time ratio: ([0-9\.]+)",
        "servers number": r"\[INFO\]\[MyClient\]\[main\] servers number: ([0-9]+)"
    }

    extracted_info = {"log_file": log_file}

    default_matches = re.findall(regex_patterns["default"], log_content)
    for match in default_matches:
        key = match[0].strip()
        value = float(match[1])
        extracted_info[key] = value

    time_ratio_match = re.search(regex_patterns["Time ratio"], log_content)
    if time_ratio_match:
        extracted_info["Time ratio"] = float(time_ratio_match.group(1))

    servers_number_match = re.search(regex_patterns["servers number"], log_content)
    if servers_number_match:
        extracted_info["servers number"] = int(servers_number_match.group(1))

    all_logs_info.append(extracted_info)

df = pd.DataFrame(all_logs_info)

df = df.sort_values(by='servers number')

df['Speedup'] = df.loc[df['servers number'] == 1, 'Time total'].values[0] / df['Time total']

df.to_csv('result.csv', index=False)

# nb - Speedup
plt.figure(figsize=(10, 6))
plt.plot(df['servers number'], df['Speedup'], marker='o')
plt.xticks(df['servers number'])
plt.xlabel('Servers Number')
plt.ylabel('Speedup')
plt.savefig('figure/Speedup.png')

# nb - ratio and total time
x = df['servers number']
y1 = df['Time total']
y2 = df['Time ratio']

fig, ax1 = plt.subplots()
fig.set_size_inches(10, 6)
plt.xticks(df['servers number'])

ax1.plot(x, y1, color='tab:blue', marker='o')
ax1.set_xlabel('Servers Number')
ax1.set_ylabel('Time Total', color='tab:blue')
ax1.tick_params(axis='y', labelcolor='tab:blue')

ax2 = ax1.twinx()
ax2.plot(x, y2, color='tab:red', marker='o')
ax2.set_ylabel('Time Ratio', color='tab:red')
ax2.tick_params(axis='y', labelcolor='tab:red')

plt.savefig('figure/dual_axis.png')
