function output = ola(w,hop,Nb)

% function output = ola(w,hop,Nb)
% realise l'addition-recouvrement de la fenetre w,
% avec un décalage hop et un nombre Nb de fenetres.
% par defaut Nb = 10;

if nargin <= 2, Nb=10;end

w = w(:).'; % vecteur ligne
N = length(w);
output = zeros(1,(Nb-1)*hop+N); % reserve l'espace memoire

for k=1:Nb;
    deb = (k-1)*hop +1;
    fin = deb+N-1;
    output(deb:fin) = output(deb:fin)+w; % OLA
end

