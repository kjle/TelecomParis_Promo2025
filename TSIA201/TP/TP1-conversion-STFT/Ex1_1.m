clc
clear all
close all
%%
filename = "caravan_48khz.wav";
[x, Fs] = audioread(filename);
[freq,Xf] = myfft(x,Fs);
figure(1);
plot(freq,Xf);

%% UP L=2
L = 2;
Fs = L*Fs;
x1 = upsample(x,L);
[freq,Xf1] = myfft(x1,Fs);
figure(2);
plot(freq,Xf1);

%% LPF H

[h delta] = firpm(55,[0 15e3 25e3 Fs]/Fs,[1 1 0 0]);
fvtool(h,1);
%% filter

x2 = filter(h,1,x1);
[freq, Xf2] = myfft(x2,Fs);
figure(3);
plot(freq,Xf2);

%% DOWN M = 3
M = 3;
Fs = Fs/M;
x3 = downsample(x2,M);
[freq,Xf3] = myfft(x3,Fs);
figure(4);
plot(freq,Xf3);

%%
figure(5);
subplot(2,1,1)
plot(x);
subplot(2,1,2)
plot(x3)