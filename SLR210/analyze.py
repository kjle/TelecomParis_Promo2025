import os
import re

summary_path = 'summary'

param_pattern = re.compile(r'N=(\d+)_f=(\d+)_a=([0-9.]+)_tle=(\d+)\.txt')
result_pattern = re.compile(r'Time for first process to decide: ([0-9.]+)ms')

results = {}

for filename in os.listdir(summary_path):
    if filename.endswith('.txt'):
        match = param_pattern.match(filename)
        if match:
            N, f, a, tle = match.groups()
            file_path = os.path.join(summary_path, filename)
            
            with open(file_path, 'r') as file:
                first_line = file.readline()
                result_match = result_pattern.match(first_line)
                if result_match:
                    TT = float(result_match.group(1))
                    results[(N, f, a, tle)] = TT

# for key, value in results.items():
#     print(f'N={key[0]}, f={key[1]}, a={key[2]}, tle={key[3]}: {value}ms')
                    
# 保存results字典中的数据
import pickle
from datetime import datetime
timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
filename = f'savedatas/results_{timestamp}.pickle'
with open(filename, 'wb') as file:
    pickle.dump(results, file)

