import re
# define paths
log_file_path = "logs/log.txt"
info_output_path = "logs/info.txt"
debug_output_path = "logs/debug.txt"
param_file_path = "param.txt"

param_content = ""
with open(param_file_path, 'r') as param_file:
    param_content = param_file.read()
pattern = re.compile(r'(\w+)\s*=\s*(\d+\.?\d*)')
parameters = {}
for match in pattern.finditer(param_content):
    param_name = match.group(1)
    param_value = match.group(2)
    if '.' in param_value:
        param_value = float(param_value)
    else:
        param_value = int(param_value)
    parameters[param_name] = param_value
summary_output_path = "summary/N=" + str(parameters['N']) + "_f=" + str(parameters['CRASH_NUMBER']) + "_a=" + str(parameters['CRASH_PROBABILITY']) + "_tle=" + str(parameters['LEADER_ELECTION_TIMEOUT']) + ".txt"

info_count = 0

pattern1 = re.compile(r'\[INFO\].*\[akka:\/\/system\/user\/Actor(\d+)\].*DECIDE from.*proposal \[(\d+)\]')
pattern2 = re.compile(r'\[INFO\].*\[akka:\/\/system\/user\/Actor\d+\].*Process \[(\d+)\].*value \[(\d+)\] ballot.*: (\d+)ms')

decide_value = -1
decide_value_write = False
concurrency = True

node_number = set()
node_info = {}

time_sum = 0
time_count = 0

with open(info_output_path, 'w') as info_file, open(debug_output_path, 'w') as debug_file, open(summary_output_path, 'w') as summary_file:
    with open(log_file_path, 'r') as log_file:
        for line in log_file:
            if line.startswith('[INFO]') and info_count >= 8:
                info_file.write(line)
                match1 = pattern1.search(line)
                match2 = pattern2.search(line)
                if match1:
                    actor =int(match1.group(1))
                    value = match1.group(2)                        
                    if (decide_value_write == False and decide_value == -1):
                        decide_value = value
                        decide_value_write = True
                    if (decide_value != value):
                        node_info[actor] = f"NODE\t[{actor}]\tVALUE\t[{value}]\t<------------------ uncheck\n"
                        concurrency = False
                    else:
                        if actor not in node_number:
                            node_info[actor] = f"NODE\t[{actor}]\tVALUE\t[{value}]\n"
                    node_number.add(actor)
                elif match2:
                    process = int(match2.group(1))
                    value = match2.group(2)
                    time = match2.group(3)
                    if (decide_value_write == False and decide_value == -1):
                        decide_value = value
                        decide_value_write = True
                    if decide_value != value :
                        node_info[process] = f"NODE\t[{process}]\tVALUE\t[{value}]\tTIME[{time}ms]\t<---------- uncheck\n"
                        concurrency = False
                    else:
                        if process not in node_number:
                            node_info[process] = f"NODE\t[{process}]\tVALUE\t[{value}]\tTIME[{time}ms]\n"
                            if time_count == 0: 
                                time_sum = int(time)
                                time_count += 1
                    node_number.add(process)
            elif line.startswith('[DEBUG]'):
                debug_file.write(line)
            if line.startswith('[INFO]'):
                info_count += 1
    if concurrency == False:
        summary_file.write(f"**************[CONCURRENCY ERREOR]*************\n")
    summary_file.write(f"Time for first process to decide: {time_sum}ms\n")
    summary_file.write(f"**************[PARAM INFO]************\n")
    with open(param_file_path, 'r') as param_file:
        for line in param_file:
            summary_file.write(line)
    summary_file.write(f"**************[NODE INFO]*************\n")
    for node_line in sorted(node_info):
        summary_file.write(node_info[node_line])

