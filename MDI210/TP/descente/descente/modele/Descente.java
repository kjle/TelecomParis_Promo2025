package descente.modele;

import java.util.Observable;

import probleme.Pb;

/**
 * Classe principale qui contient la methode de descente
 */
public class Descente extends Observable implements Runnable  {

	private double seuil = 0.00001; // si le gradient a une longueur inferieure a seuil, la methode de descente s'arrete
	private Pb pb;  			// le probleme traite
	private Couple P = null;    // doit contenir le point courant du plan dans la methode de descente
	private Domaine domaine;    // le domaine dans lequel on cherche le minimum de la fonction
	private int nbPas = 0;      // sert a  compter le nombre de pas de la methode de descente
	private Couple direction;   // pour la direction que s'apprÃªte Ã Â  suivre la methode de descente
	private boolean suspendre;  // sert a suspendre la descente
	private boolean finie;      // vaut faux pendant que la descente s'effectue
	private boolean stoppee;	// passe a  vrai si la methode de descente est interrompue par l'utilisateur
	private boolean atteintMinimum = true;  // passe a faux si la methode de descente montre que le problÃ¨me n'est pas borne
	private int delai = 2000;    /* sert a ralentir la descente pour l'affichage graphique ; 
								    delai en millisecondes entre deux etapes de la descente */

	public static double epsilon = 1E-12;

	/**
	 * Un double est considere comme valant 0 si sa valeur absolue est inferieure a epsilon
	 * @param v le double considere
	 * @return true si le nombre est considere comme nul, false dans le cas contraire
	 */
	public static boolean estNul(double v) {
		return v < epsilon && v > -epsilon;
	}

	/**
	 * @param pb le probleme considere ; le probleme consiste toujours a  chercher le minimum
	 *  d'une fonction convexe de deux variables sur un domaine du plan limite par des droites.
	 */
	public Descente(Pb pb)  {
		this.pb = pb;
		this.domaine = pb.getDomaine();
	}

	/**
	 * Fait trois actions : 
	 * <br>- notifie les observateurs, ici l'interface graphique, afin que celle-ci actualise l'affichage 
	 * <br>- suspend le thread en cours d'execution pendant un nombre minimum de millisecondes egal a la valeur de delai
	 * <br>- si la methode est suspendue, attend jusqu'a recevoir une notification quand la methode n'est plus 
	 * suspendue.  
	 */
	public synchronized void prevenirEtAttendre() {
		this.setChanged();
		this.notifyObservers();
		try {
			this.wait(delai);
			while (suspendre) wait();
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}	
	}

	/**
	 * La methode de descente est lancee dans un thread faisant tourner cette methode run.
	 */
	public void run() {
		if (P != null) effectuer();
	}

	/**
	 * Effectue la descente a partir du point P.
	 */
	public void effectuer() {
		// un passage dans la boucle pour chaque étape de la méthode de descente
		do {
			this.prochaineDirection();
			this.prevenirEtAttendre(); 
			if (!this.finie)	 {
				this.P = this.pointSuivantDescente();
				if (this.P == null) { // la fonction f n'atteint pas de minimum sur le domaine
					this.atteintMinimum = false;	
					this.finie = true;
				}
			}
			nbPas++;
		} while (!this.finie && !this.stoppee);
		this.prevenirEtAttendre(); 
	}


	/**************************************************************
	 * METHODE A IMPLEMENTER 
	 * Recherche la prochaine direction a suivre lorsque le point courant P
	 * est dans l'interieur strict du domaine.
	 * Si la descente n'est pas finie, met la prochaine direction dans l'attribut this.direction  
	 * (c'est obligatoire d'utiliser cet attribut).
	 *<br> ATTENTION : si la methode de descente est finie, la methode doit passer this.finie a true.
	 * @see Couple#norme()
	 * @see Couple#mult(double)
	 * @see Pb#gradientf(Couple)
	 * @see #seuil
	 */
	private void directionASuivreSiInterieur() {
		// find the grandient of f at P
		Couple direction = pb.gradientf(this.P);
		// if the norm of the gradient is less than the threshold, stop the descent
		if (direction.norme() <= this.seuil) {
			this.direction = new Couple(0, 0);
			this.finie = true;
		} else {
			// otherwise, set the direction opposite to the gradient
			this.direction = direction.mult(-1);
		}
	}

	/***********************************************************
	 * METHODE A IMPLEMENTER 
	 * Recherche la prochaine direction a suivre lorsque le point courant P
	 * est sur un bord du domaine mais pas dans un coin.
	 * Si la descente n'est pas finie, met la prochaine direction dans l'attribut this.direction  
	 * (c'est obligatoire d'utiliser cet attribut).
	 *<br>Selon la direction du gradient de f et si la descente n'est pas terminée, la prochaine direction
	 * peut etre vers l'intérieur strict du domaine ou le long du bord dans un sens ou dans l'autre.
	 *<br> Attention : si la methode de descente est finie, la methode doit passer this.finie a true.
	 * @see Couple#norme()
	 * @see Couple#produitScalaire(Couple)
	 * @see Couple#estPerpendiculaire(Couple)
	 * @see Couple#mult(double)
	 * @see Contrainte#getGradient()
	 * @see Contrainte#getVecteurUnitaireBord()
	 * @see Pb#gradientf(Couple)
	 * @see #seuil * 
	 * @param c la contrainte saturee par le point courant P
	 */
	private void directionASuivreSiBord(Contrainte c) {
		// find the gradient of f at P
		Couple gradient = pb.gradientf(this.P);
		// if the norm of the gradient is less than the threshold, stop the descent
		if (gradient.norme() <= this.seuil) {
			this.direction = new Couple(0, 0);
			this.finie = true;
		} else {
			// get gradient of the constraint
			Couple gradientContrainte = c.getGradient();
			
			// if the grandient of P pointer to the outside of the constraint, set the direction opposite to the gradient
			if (gradient.produitScalaire(gradientContrainte) > 0) {
				this.direction = gradient.mult(-1);
			} else {
				// get the unit vector of the constraint
				Couple vecteurUnitaireBord = c.getVecteurUnitaireBord();
				// if the gradient of P is perpendicular to the constraint, stop the descente
				if (gradient.estPerpendiculaire(vecteurUnitaireBord)) {
					this.direction = new Couple(0, 0);
					this.finie = true;
				} else {
					// if the gradient of P is at the same direction of unite direction of the constraint
					if (gradient.produitScalaire(vecteurUnitaireBord) > 0) {
						this.direction = vecteurUnitaireBord.mult(-1);
					} else {
						this.direction = vecteurUnitaireBord.mult(1);
					}
				}
			}
		}
	}
	
	/*************************************************************
	 * METHODE A IMPLEMENTER 
	 * Recherche la prochaine direction a suivre lorsque le point courant P
	 * est dans un coin du domaine.
	 * Si la descente n'est pas finie, met la prochaine direction dans l'attribut this.direction  
	 * (c'est obligatoire d'utiliser cet attribut).
	 *<br> ATTENTION : si la methode de descente est finie, la methode doit passer this.finie a true.
	 * <br> Voir le descriptif du TP (https://perso.telecom-paristech.fr/hudry/optim/descente/index.html)
	 * La methode decompose etant statique, on l'appelle avec Couple.decompose...
	 * @see Couple#norme()
	 * @see Couple#produitScalaire(Couple)
	 * @see Couple#mult(double)
	 * @see Couple#decompose(Couple, Couple, Couple)
	 * @see Contrainte#getGradient()
	 * @see Contrainte#getVecteurUnitaireBord()
	 * @see Pb#gradientf(Couple)
	 * @param coin un tableau a deux cases pour les deux contraintes saturees par le point courant P
	 */
	private void directionASuivreSiCoin(Contrainte[] coin) {
		// find the gradient of f at P
		Couple direction = pb.gradientf(this.P);
		// find the gradient of the first constraint
		Couple n1 = coin[0].getGradient().mult(-1);
		// find the gradient of the second constraint
		Couple n2 = coin[1].getGradient().mult(-1);
		// find the unit vector of the first constraint
		Couple u1 = coin[0].getVecteurUnitaireBord();
		// find the unit vector of the second constraint
		Couple u2 = coin[1].getVecteurUnitaireBord();
		// find the decomposition of the gradient of P on the n1 and n2
		Couple decompositionNorm = Couple.decompose(direction, n1, n2);
		// find the decosmposition of the gradient of P on the u1 and u2
		Couple decompositionUnite = Couple.decompose(direction, u1, u2);

		// if the norm of the gradient is less than the threshold, stop the descent
		if (direction.norme() <= this.seuil) {
			this.direction = new Couple(0, 0);
			this.finie = true;
		} else {
			// case 1, 2 and 3
			if (decompositionUnite.x <= 0 && decompositionUnite.y <= 0) {
				this.direction = direction.mult(-1);
			} else if (decompositionNorm.x >= 0 && decompositionNorm.y >= 0) {
				this.direction = new Couple(0, 0);
				this.finie = true;
			} else {
				this.direction = (u1.produitScalaire(direction) <= u2.produitScalaire(direction) ? u1 : u2);
			}
		}
	}
	
	/******************************************************
	 * METHODE A IMPLEMENTER 
	 * On considere une demi-droite parametree par t -> pointDepart + t.dir (t >= 0) ;
	 * on pose phi(t) = f(pointDepart + t.dir) ; on suppose que l'on a phi'(0) < 0 ; 
	 * on cherche un point P = pointDepart + t.dir, t > 0, avec phi'(t) > 0. 
	 * On rappelle que, par hypothese, la fonction f est convexe, ce qui entraine que phi' est croissante.
	 * On pourra d'abord tester t = 1 puis, si necessaire, doubler la valeur de t successivement.
	 * La derivee de  phi en t s'ecrit : pb.phiDerivee(pointDepart, dir, t);
	 * @see Pb#phiDerivee(Couple, Couple, double)
	 * @param pointDepart l'origine de la demi-droite.
	 * @param dir la direction de la demi-droite.
	 * @return si on ne trouve pas de tel point avec t < Double.MAX_VALUE/2, la methode retourne -1 ;
	 *         <br>sinon la methode retourne une valeur de t avec phi'(t) > 0.
	 */
	public double chercheSecondPoint(Couple pointDepart, Couple dir) {
		// Suppose phi'(0) < 0, and start from t = 1
		double t = 1;
		// the derivative of phi at the point found so far
		double phiPrimeTmp = pb.phiDerivee(pointDepart, dir, t);
		while(!(phiPrimeTmp > 0)){
			t *= 2;
			if (t >= Double.MAX_VALUE) {// we can't find a point with t < Double.MAX_VALUE
				return -1;
			}
			// the derivative of phi at the new point
			phiPrimeTmp = pb.phiDerivee(pointDepart, dir, t);
		}
		return t;
	}
	
	/***************************************************** 
	 * METHODE A IMPLEMENTER
	 * On considere une demi-droite parametree par  t -> pointDepart + t.dir (t >= 0) ;  
	 * On pose phi(t) = f(pointDepart + t.dir) ; on suppose que l'on a phi'(0) < 0 et phi'(t1) > 0 ; 
	 * On cherche un point P = pointDepart + t * dir 
	 * entre pointDepart et pointDepart + t1 * dir avec phi'(t) = 0. 
	 * Pour cela, on procede par dichotomie.
	 * <br> On peut utiliser la methode statique estNul de cette classe pour tester si une valeur 
	 * de type double est nulle ou non.
	 * La derivee de  phi en t s'ecrit : pb.phiDerivee(pointDepart, dir, t);
	 * @see Couple#mult(double)
	 * @see Couple#ajoute(Couple)
	 * @see Descente#estNul(double) //methode statique
	 * see Pb#phiDerivee(Couple, Couple, double)
	 * @param pointDepart l'origine de la demi-droite.
	 * @param dir la direction de la demi-droite.
	 * @param t1 parametre tel que phi'(t1) > 0.
	 * @return le point P = pointDepart + t * dir tel que phi'(t) = 0 (ou presque). 
     */
	public Couple dichotomie(Couple pointDepart, Couple dir, double t1) {		
		// suppose phi'(0) < 0 and phi'(t1) > 0
		double l = 0;
		double r = t1;
		double phiPrimeM = pb.phiDerivee(pointDepart, dir, (l + r) / 2);
		// while phi'(t) is not zero
		while (!estNul(phiPrimeM)) {
			if (phiPrimeM > 0) {
				r = (l + r) / 2;
			} else if (phiPrimeM < 0){
				l = (l + r) / 2;
			}
			phiPrimeM = pb.phiDerivee(pointDepart, dir, (l + r) / 2);
		}
		return pointDepart.ajoute(dir.mult((l + r) / 2));
	}
	
	/*************************************************
	 * METHODE A IMPLEMENTER
	 * Verifie qu'il s'agit bien d'un minimum en utilisant la condition de Karush, Kuhn et Tucker 
	 * @param P	Le point ou on verifie qu'il s'agit d'un minimum.
	 * @see Domaine#estCoin(Couple)
	 * @see Domaine#estSurBord(Couple)
	 * @see Couple#norme()
	 * @see Couple#produitScalaire(Couple)
	 * @see Couple#estPerpendiculaire(Couple)
	 * @see Couple#decompose(Couple, Couple, Couple)
	 * @see Contrainte#getVecteurUnitaireBord()
	 * @see Contrainte#getGradient()
	 * @see Pb#gradientf(Couple)
	 * @return  	null si la condition de Karush, Kuhn et Tucker n'est pas verifiee
	 * 				<br>sinon
	 * 					<br>- le couple des multiplicateurs de Lagrange si P est sur un coin
	 * 					<br>- le couple forme par le multiplicateur de Lagrande et 0 si P est sur un bord
	 * 					<br>- (0, 0) si P est a l'interieur
	 */
	public Couple KarushKuhnTucker(Couple P) {
		Couple mu = null;
		Couple gradientCurrentPoint = pb.gradientf(P);

		// case 1 : P is in the corner
		if(this.domaine.estCoin(P)!=null){
			// get the two constraints on this point
			Contrainte[] coin = this.domaine.estCoin(P);
			// suppose only two constraints at this point
			// get the gradient of the two constraints
			Couple gradientContrainte1 = coin[0].getGradient();
			Couple gradientContrainte2 = coin[1].getGradient();
			// find the decomposition
			Couple decompositionContrainte = Couple.decompose(gradientCurrentPoint, gradientContrainte1, gradientContrainte2);
			if(decompositionContrainte.x < 0 && decompositionContrainte.y < 0){
				// if the two decompositions are both less than 0, KKT satisfied
				// otherwise KKT is not satisfied
				mu = decompositionContrainte.mult(-1);
			}
		// case 2 : P is on the edge
		}else if(this.domaine.estSurBord(P) != null){
			// get the constraint on this point			
			Contrainte c = this.domaine.estSurBord(P);
			Couple vecteurUnitaireBordConstrainte = c.getVecteurUnitaireBord();
			Couple gradientContrainte = c.getGradient();
			
			if(gradientCurrentPoint.estPerpendiculaire(vecteurUnitaireBordConstrainte)) {
				// if the gradient of the current point is parallel to the constraint, KKT satisfied
				// otherwise KKT is not satisfied
				double rt = -gradientCurrentPoint.norme()/gradientContrainte.norme();
				mu = new Couple(rt, 0);
			}
		// case 3 : P is inside the domain
		}else{
			// KKT is not satisfied
			mu = new Couple(0,0);
				
		}
		return mu;	
	}
	
	/**
	 * Recherche la prochaine direction a suivre.
	 * La methode considere les cas ou :
	 *    		<br>P est a  l'interieur du domaine, 
	 *    		<br>P est sur un bord, 
	 *    		<br>P est sur un coin 
	 *<br> Si la descente est finie, la methode passe this.finie a true.
	 */
	public void prochaineDirection() {
		Contrainte c;
		Contrainte [] coin;
		c = this.domaine.estSurBord(P);
		if (c != null) {
			coin = this.domaine.estCoin(P);
			if (coin == null) {
				this.directionASuivreSiBord(c);
			}
			else {
				this.directionASuivreSiCoin(coin);
			}
		}
		else {
			this.directionASuivreSiInterieur();
		}
	}

	/**
	 * Connaissant le point courant et la direction a  suivre, 
	 * la methode recherche le point courant suivant. 		
	 * @return  
	 * this.P est le point courant (qui peut etre a l'interieur de domaine, sur un 
	 * bord du domaine ou sur un coin) et this.direction est la direction a suivre ; la methode retourne
	 *   <br>- soit le point courant suivant (qui peut etre a l'interieur du domaine, sur un bord, sur un coin)
	 *   <br>- soit null si elle a mis en evidence que la fonction f n'atteint pas de minimum 
	 * 		   sur le domaine considere.
	 */
	public Couple pointSuivantDescente() {
		double t1;
		/*
		 * Explications concernant l'instruction suivante.
		 * On considere la demi-droite parametree par  t -> this.P + t * this.direction ; 
		 * cette demi-droite peut partir de l'interieur du domaine, ou d'un bord, ou d'un coin 
		 * et peut longer un bord du domaine.
		 * Si la demi-droite est toute entiere dans le domaine, ce qui n'est possible que 
		 * si le domaine n'est pas borne,l'instruction retourne -1. 
		 * Sinon, elle retourne une valeur de t > 0 pour laquelle this.P + t * this.direction appartient 
		 * a un bord du domaine.		
		 */
		t1 = domaine.intersection(this.P, this.direction);
		if (t1 == 0) return this.P;
		// Si la demi-droite rencontre un bord du domaine et si la derivee de t -> this.P + t * this.direction
		// est positive en ce point d'intersection, on retourne ce point.
		if ((t1 > 0) && (pb.phiDerivee(this.P, this.direction, t1) <= 0)) 
			return this.P.ajoute(this.direction.mult(t1));
		
		// Si la demi-droite est toute entiere dans le domaine, on cherche un point ou la derivee de
		// t -> this.P + t * this.direction soit positive
		if (t1 < 0) {
			t1 = chercheSecondPoint(P, direction);
			// Dans le cas ci-dessous, le probleme n'atteint pas de minimum
			if (t1 < 0) return null;
			// sinon la derivee de t -> this.P + this * direction est positive pour t = t1
		}
		return dichotomie(P, direction, t1);
	}

	/**
	 * @return le probleme traite this.pb
	 */
	public Pb getPb() {
		return this.pb;
	}

	/**
	 * @return le point courant P
	 */
	public Couple getP() {
		return this.P;
	}

	/**
	 * @return la direction a suivre this.direction.
	 */
	public Couple getDirection() {
		return this.direction;
	}

	/** 
	 * permet d'initialiser le point de depart de la methode de descente
	 * @param P la valeur a  donner a  P.
	 */
	public void setP(Couple P) {
		this.P = P;
	}

	/**
	 * Permet de suspendre ou reprendre la methode de descente
	 * @param suspendre si le parametre vaut true, la methode est suspendue, elle est reprise 
	 * si le parametre vaut false.
	 */
	public void setSuspendre(boolean suspendre) {
		this.suspendre = suspendre;
	}

	/**
	 * Pour savoir si la methode de descente a ete stoppee
	 * @return la valeur de l'attribut booleen this.stopped
	 */
	public boolean isStoppee() {
		return this.stoppee;
	}

	/**
	 * Sert a  stopper la methode de descente
	 */
	public void stopper() {
		this.stoppee = true;
		this.finie = true;
	}

	/**
	 * @return true si la descente est terminee et false sinon
	 */
	public boolean isFinie() {
		return this.finie;
	}

	/**
	 * Apres que la descente soit terminee, sert a  savoir si le probleme traite
	 * atteint son minimum ou non
	 * @return true ou false selon que le probleme traite atteint son minimum ou non
	 */
	public boolean atteintSonMinimum() {
		return this.atteintMinimum;
	}

	/** 
	 * Permet de preciser le seuil de la norme du gradient 
	 * @param seuil si la norme du gradient est inferieur a  seuil, la methode de descente s'arrete
	 */
	public void setSeuil(double seuil) {
		this.seuil = seuil;
	}

	/**
	 * @return le nombre de pas effectues depuis le debut de la methode de descente
	 */
	public int getNbPas() {
		return this.nbPas;
	}

	/**
	 * @return temps d'attente entre deux pas de la methode de descente, en millisecondes
	 */
	public int getDelai() {
		return delai;
	}

	/**
	 * Permet de fixer le temps d'attente entre deux pas de la methode de descente
	 * @param delai temps d'attente entre deux pas de la methode de descente, en millisecondes
	 */
	public void setDelai(int delai) {
		this.delai = delai;
	}
}
