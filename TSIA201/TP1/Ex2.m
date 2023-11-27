clc
clear all
close all
%%
Nw = 64;
M = 128;
w = hanning(Nw);
W = fft(w,M);
f = 1:M/2+1;
plot(f,10*log10(abs(W(1:M/2+1))));
%%
filename = "caravan_48khz.wav";
[x, Fs] = audioread(filename);


