% analyse d'un signal � l'aide de la TFCT
% transform�e de Fourier � Court Terme
clear all; close all;

[x,Fe] = audioread('caravan_48khz.wav');
x = x(1:10*Fe,1);x = x(:); % ainsi x est un vect. colonne
        % monovoie (voie gauche si st�r�o)

N = length(x); % longueur du signal
Nw = 512;
w = hanning(Nw); % d�finition de la fenetre d'analyse
ws = w; % d�finition de la fen�tre de synth�se
R = Nw/4; % incr�ment sur les temps d'analyse,
          % appel� hop size, t_a=uR
M = 512; % ordre de la tfd
L = M/2+1;

affich = 1 ; % pour affichage du spectrogramme, 0 pour
             % pour faire analyse/modif/synth�se sans affichage
             % note: cf. spectrogram sous matlab


Nt = fix( (N-Nw)/R ); % calcul du nombre de tfd � calculer
y = zeros(N,1); % signal de synth�se

if affich
    Xtilde = zeros(M,Nt);
end

for u=1:Nt;  % boucle sur les trames
   deb = (u-1)*R +1; % d�but de trame
   fin = deb + Nw -1; % fin de trame
   tx = x(deb:fin).*w; % calcul de la trame  
   X = fft(tx,M); % tfd � l'instant b
   
   if affich, Xtilde(:,u)=X;end
   
   % op�rations de transformation (sur la partie \nu > 0)
   % ....
   Y = X;
   % fin des op�ration de transformation
   
   % resynth�se
   % overlap add
end



soundsc(y,Fe)

if affich
    
freq = (0:M/2)/M*Fe;
b = [0:Nt-1]*R/Fe+Nw/2;
imagesc(b,freq,db(abs(Xtilde(1:L,:))))
axis xy

end