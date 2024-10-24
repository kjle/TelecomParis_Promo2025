clc
clear all
close all
%%
filename = "caravan_48khz.wav";
[x, Fs] = audioread(filename);
[freq,Xf] = myfft(x,Fs);
figure(1);
plot(freq,Xf);
%% diag 1
t_start = tic;
x1 = upsample(x,2);
x2 = x1(2:end);
x3 = downsample(x2,3);
t_stop = toc(t_start)

%% diag 2
t_start = tic;
y1 = [0;x];
y2 = downsample(y1,3);
y3 = upsample(y2,2);
y4 = y3(2:end);
t_stop = toc(t_start)

%% plot
figure;
plot(x3-y4)

%% cascade of efficient structure
% down 3
s11 = downsample(x,2);
s12 = downsample(s11,3);
s21 = downsample(x(2:end),2);
s22 = downsample(s21,3);
% s = s12 + [0;s22(1:end-1)];
s = s12 + s22;
r11 = upsample(s,2);
r12 = upsample(r11,2);
r21 = upsample(s,2);
r22 = upsample(r21,2);
r = r12(2:end) + r22(1:end-1);
%% plot
figure;
plot(r12(2:end)); hold on; plot(r22)

