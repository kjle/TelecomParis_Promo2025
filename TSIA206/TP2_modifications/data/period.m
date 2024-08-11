function [P,voiced] = period(x,Fs,Pmin,Pmax,seuil);
% [P,voiced] = period(x,Fs,Pmin,Pmax,seuil);
% If voiced = 1, P is the period signal x expressed in number of samples
% If voiced = 0, P is equal to 10ms.Fs

x = x(:);
x = x - mean(x);
N = size(x,1);

if nargin<5, seuil = 0.7; end;
if nargin<4, Pmax = 1/80; end;
if nargin<3, Pmin = 1/300; end;

Nmin = 1 + ceil(Pmin*Fs);
Nmax = 1 + floor(Pmax*Fs);
Nmax = min([Nmax,N]);

Nfft = 2^ nextpow2(2*N-1);
X = fft(x,Nfft);
S = X .* conj(X) / N;
r = real(ifft(S));

[rmax,I] = max(r(Nmin:Nmax));
P = (I+Nmin-2);
corr = (rmax/r(1)) * (N/(N-P));
voiced = corr > seuil;
if ~voiced, P = round(10e-3*Fs); end;
