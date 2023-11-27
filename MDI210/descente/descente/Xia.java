package descente.modele;

import java.util.Observable;

import probleme.Pb;

/**
 * Classe principale qui modelise la methode de descente
 */
public class Xia extends Observable implements Runnable  {

	private double seuil = 0.00001; // si le gradient a une longueur inferieure a seuil, la methode de descente s'arrete
	private Pb problemeTraite;  // le probleme traite
	private Couple pointCourant = null;    // doit contenir le point courant du plan dans la methode de descente
	private Domaine domaine;    // le domaine dans lequel on cherche le minimum de la fonction
	private int nbPas = 0;      // sert a  compter le nombre de pas de la methode de descente
	private Couple directionASuivre;   // pour la direction que s'apprete a  suivre la methode de descente
	private boolean suspendre;  // sert a suspendre la descente
	private boolean finie;       // vaut faux pendant que la descente s'effectue
	private boolean stoppee;	// passe a  vrai si la methode de descente est interrompue par l'utilisateur
	private boolean atteintMinimum = true;  // passe a faux si la methode de descente montre que le probleme n'est pas borne
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
	 * @param problemeTraite le probleme considere ; le probleme consiste toujours a  chercher le minimum
	 *  d'une fonction convexe de deux variables sur un domaine du plan limite par des demi-droites.
	 */
	public Descente(Pb problemeTraite)  {
		this.problemeTraite = problemeTraite;
		this.domaine = problemeTraite.getDomaine();
	}

	/**
	 * Fait trois actions : 
	 * <br>- notifie les observateurs, ici l'interface graphique, afin que celle-ci actualise l'affichage 
	 * <br>- suspend le thread en cours d'execution pendant un nombre minimum de millisecondes egal a la valeur de delai
	 * <br>- si la methode est suspendue, attend jusqu'a  recevoir une notification alors que la methode n'est plus 
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
		if (this.pointCourant != null) effectuer();
	}

	/**
	 * Effectue la descente a  partir du point P.
	 */
	public void effectuer() {
		// un passage dans la boucle pour chaque etape de la methode de descente
		do {
			this.prochaineDirection();
			this.prevenirEtAttendre(); 
			if (!this.finie)	 {
				this.pointCourant = this.pointSuivantDescente();
				if (this.pointCourant == null) { // la fonction f n'atteint pas de minimum sur le domaine
					this.atteintMinimum = false;	
					this.finie = true;
				}
			}
			nbPas++;
		} while (!this.finie && !this.stoppee);
		this.prevenirEtAttendre(); 
	}

	/*/**<font color="red">
	 * METHODE A IMPLEMENTER Recherche la prochaine direction a suivre lorsque le point courant, 
	 * mis dans l'attribut pointCourant, 
	 * est dans l'interieur strict du domaine.
	 * problemeTraite.gradientf(pointCourant) donne le gradient de la fonction considere dans le point courant, 
	 * le resultat est de type Couple.
	 * Si le gradient de la fonction au point courant a une norme (calcule en utilisant la methode norme()
	 *  de la classe Couple) inferieure a this.seuil, on considere que la methode est finie.
	 * Si la descente n'est pas finie, met la prochaine direction dans l'attribut this.directionASuivre  
	 * (c'est obligatoire d'utiliser cet attribut).
	 * La methode mult de la classe Couple permet de multiplier une variable de type  Couple par un scalaire.
	 *<br> ATTENTION : si la methode de descente est finie, la methode doit passer this.finie a true.
	 * @see Couple#norme()
	 * @see Couple#mult(double) 
	 * @see Pb#gradientf(Couple)
	 * @see #seuil
	 </font>*/
	private void directionASuivreSiInterieur() {
		
		// find the gradient of the current point
		Couple direction = problemeTraite.gradientf(this.pointCourant);
		
		// compare the norm of the gradient with the seuil
		if(direction.norme()>seuil){
			this.directionASuivre = direction.mult(-1);
			
		}else{
			// if the norm is too small, finish the program.
			this.directionASuivre = new Couple(0,0);
			this.finie = true;
		}
	}

	/**<font color="red">
	 * METHODE A IMPLEMENTER Recherche la prochaine direction a suivre lorsque le point courant 
	 * est sur un bord du domaine mais pas dans un coin.
	 * Si la descente n'est pas finie, met la prochaine direction dans l'attribut this.directionASuivre  
	 * (c'est obligatoire d'utiliser cet attribut).
	 *<br>Selon la direction du gradient de f et si la descente n'est pas terminee, la prochaine direction
	 * peut etre vers l'interieur strict du domaine ou le long du bord dans un sens ou dans l'autre.
	 * Voir dans la page décrivant le sujet les explications sur les methodes citées ci-dessous.
	 *<br> ATTENTION : si la methode de descente est finie, la methode doit passer this.finie a true.
	 * @see Couple#norme()
	 * @see Couple#produitScalaire(Couple)
	 * @see Couple#estPerpendiculaire(Couple)
	 * @see Couple#mult(double)
	 * @see Contrainte#getGradient()
	 * @see Contrainte#getVecteurUnitaireBord()
	 * @see Pb#gradientf(Couple)
	 * @see #seuil * 
	 * @param c la contrainte saturee par le point courant 
	 </font>*/
	private void directionASuivreSiBord(Contrainte c) {
		
		// find the gradient of the current point
		Couple direction = problemeTraite.gradientf(this.pointCourant);
		
		// compare the norm of the gradient with the seuil
		if(direction.norme()>seuil){
			
			// get the gradient of the constraint
			Couple gradientContrainte = c.getGradient();
			
			if(direction.produitScalaire(gradientContrainte)>0){
				//// the gradient of the current point has a direction to the outer domain
				this.directionASuivre = direction.mult(-1);
			}else{
				// get the unitary vector of the bord
				Couple bordUni = c.getVecteurUnitaireBord();
				
				if(direction.estPerpendiculaire(bordUni)){
					//// if the gradient of the current point has a direction to the inner domain 
					//// and is perpendicular to the bord
					this.directionASuivre = new Couple(0,0);
					this.finie = true;
				}else{
					//// the gradient has a direction to the inner but not perpendicular to the bord
					// next direction is one of the directions of the bord
					if(direction.produitScalaire(bordUni)>0){
						this.directionASuivre = bordUni.mult(-1);
						
					}else{
						this.directionASuivre = bordUni;
					}
				}
			}
		}else{
			this.directionASuivre = new Couple(0,0);
			this.finie = true;
		}
	}
	
	/**<font color="red">
	 * METHODE A IMPLEMENTER Recherche la prochaine direction a suivre lorsque le point courant 
	 * est dans un coin du domaine.
	* Si la descente n'est pas finie, met la prochaine direction dans l'attribut this.directionASuivre  
	 * (c'est obligatoire d'utiliser cet attribut).
	 *<br> ATTENTION : si la methode de descente est finie, la methode doit passer this.finie a true.
	 * @see Couple#norme()
	 * @see Couple#produitScalaire(Couple)
	 * @see Couple#mult(double)
	 * @see Contrainte#getGradient()
	 * @see Contrainte#getVecteurUnitaireBord()
	 * @see Pb#gradientf(Couple)
	 * @param coin un tableau a deux cases pour les deux contraintes saturees par le point courant 
	 </font>*/
	private void directionASuivreSiCoin(Contrainte[] coin) {
		
		// find the gradient of the current point
		Couple direction = problemeTraite.gradientf(this.pointCourant);
		
		Couple n1 = coin[0].getGradient().mult(-1);
		Couple n2 = coin[1].getGradient().mult(-1);
		Couple u1 = coin[0].getVecteurUnitaireBord();
		Couple u2 = coin[1].getVecteurUnitaireBord();
		Couple dcgrad = Couple.decompose(direction, n1, n2);
		double p1 = u1.produitScalaire(direction);
		double p2 = u2.produitScalaire(direction);
		
		if(direction.norme()>seuil){
			// Premier cas:
			if ((n1.produitScalaire(direction)<=0) && (n2.produitScalaire(direction)<=0)){
				this.directionASuivre = direction.mult(-1);
			}// Deuxieme cas:
			else if((dcgrad.x>=0) && (dcgrad.y)>=0){
				this.directionASuivre = new Couple(0,0);
				this.finie = true;
			}// Troisieme cas:
			else {
				if (p1<=p2){
					this.directionASuivre = u1;
				}else 
					this.directionASuivre = u2;
			}			
		}else{
			this.directionASuivre = new Couple(0,0);
			this.finie = true;
		}
	}

	/**<font Color ="red">
	 * METHODE A IMPLEMENTER ; cette méthode sert quand la demi-droite issue du point courant et partant dans 
	 * directionASuivre est tout entière dans le domaine. En suivant cette direction, la fonction f commence par
	 *  decroitre ; on cherche dans cette direction un point ou la fonction f croit. 
	 * On considere la demi-droite parametree par t -> pointDepart + t * dir (t >= 0) ;
	 * on pose g(t) = f(pointDepart + t * dir) ; on suppose que l'on a g'(0) < 0 ; 
	 * on cherche un point P = pointDepart + t * dir, t > 0, avec g'(t) > 0. 
	 * On rappelle que, par hypothese, la fonction f est convexe, ce qui entraine que g' est croissante.
	 * On pourra d'abord tester t = 1 puis, si necessaire, doubler la valeur de t successivement.
	 * @param pointDepart l'origine de la demi-droite.
	 * @param dir la direction de la demi-droite.
	 * @return si on ne trouve pas de tel point avec t < Double.MAX_VALUE, la methode retourne -1 ;
	 *         <br>sinon la methode retourne une valeur de t avec g'(t) > 0.
	 </font>*/
	public double chercheSecondPoint(Couple pointDepart, Couple dir) {
		
		// we suppose that the g'(0) <0
		if(problemeTraite.phiDerivee(pointDepart, dir, 0)>0){
			System.out.println("Error! we don't have g'(0) < 0");
		}		
		double t = 1;

		// the derivative of g at the point found so far
		double gprimeTmp = problemeTraite.phiDerivee(pointDepart, dir, t);
		
		// increment t until g'(t) > 0 is found
		while(gprimeTmp<0){
			t *= 2;
			if(t >= Double.MAX_VALUE){// we can't find a point with t < Double.MAX_VALUE
				return -1;
			}
			// the derivative of g at the new point
			gprimeTmp = problemeTraite.phiDerivee(pointDepart, dir, t);
		}
		return t;
		// La valeur de retour est a  modifier
		/*System.out.println("Methode chercherSecondPoint a implementer");
		System.exit(0);
		return 0;*/
	}
	
	/** <font Color ="red">
	 * METHODE A IMPLEMENTER ; on suppose qu'en partant de pointCourant  dans la 
	 * direction a suivre, la fonction f commence par décroitre puis croit. On cherche un point de
	 * minimum local de la restriction de f a la demi-droite parcourue.
	 * On considere une demi-droite parametree par  t -> pointDepart + t * dir (t >= 0) ;  
	 * on pose g(t) = f(pointDepart + t * d) ; on suppose que l'on a g'(0) < 0 et g'(t1) > 0 ; 
	 * on cherche un point P = pointDepart + t * dir entre pointDepart et pointDepart + t1 * dir avec g'(t) = 0. 
	 * Pour cela, on procede par dichotomie.
	 * <br> On peut utiliser la methode statique estNul de cette classe pour tester si une valeur 
	 * de type double est nulle ou non.
	 * @see Pb#phiDerivee(Couple, Couple, double)
	 * @see Descente#estNul
	 * @see Couple#ajoute(Couple)
	 * @param pointDepart l'origine de la demi-droite.
	 * @param dir la direction de la demi-droite.
	 * @param t1 parametre tel que g'(t1) > 0.
	 * @return le point P = pointDepart + t * dir tel que g'(t) = 0. 
     </font>*/
	public Couple dichotomie(Couple pointDepart, Couple dir, double t1) {
		double tp0 = 0;
		double tp1 = t1;
		
		// we suppose that g'(0) < 0 and g'(t1) > 0
		double gprimet0 = problemeTraite.phiDerivee(pointDepart, dir, tp0);
		double gprimet1 = problemeTraite.phiDerivee(pointDepart, dir, tp1);
		if(!(gprimet0<0 && gprimet1>0)){
			System.out.println("Error! we don't have g'(0) < 0 and g'(t1) > 0");
		}
		// find g'() at the first middle point
		double gprimetp = problemeTraite.phiDerivee(pointDepart, dir, (tp0+tp1)/2);
		// loop until g'(t) = 0 has found
		while(!estNul(gprimetp)){
			if (gprimetp>0){
				// replace the right point with the middle point
				tp1 = (tp0+tp1)/2;
			}else if (gprimetp<0){
				// replace the left point with the middle point
				tp0 = (tp0+tp1)/2;
			}
			gprimetp = problemeTraite.phiDerivee(pointDepart, dir, (tp0+tp1)/2);
		}
		
		// La valeur de retour est a modifier
		// la derivee de  t -> pointDepart + t.dir en t s'ecrit : problemeTraite.phiDerivee(pointDepart, dir, t);
		return pointDepart.ajoute(dir.mult((tp0+tp1)/2));
		//return pointDepart;
	}
	
	/**<font color="red">
	 * METHODE A IMPLEMENTER pour verifier la condition de (Karush) Kuhn et Tucker.
	 * Verifie qu'il s'agit bien d'un minimum en utilisant la condition de Kuhn et Tucker 
	 * @param P	Le point dans lequel on verifie qu'il s'agit d'un minimum.
	 * @see Domaine#estCoin(Contrainte, Contrainte)
	 * @see Domaine#estSurBord(Couple)
	 * @see Couple#norme()
	 * @see Couple#produitScalaire(Couple)
	 * @see Couple#estPerpendiculaire(Couple)
	 * @see Couple#decompose(Couple, Couple, Couple)
	 * @see Contrainte#getVecteurUnitaireBord()
	 * @see Contrainte#getGradient()
	 * @see Pb#gradientf(Couple)
	 * @return  	null si la condition de Kuhn et Tucker n'est pas verifiee
	 * 				<br>sinon
	 * 					<br>- le couple des multiplicateurs de Lagrange si on est sur un coin
	 * 					<br>- le couple forme par le multiplicateur de Lagrande et 0 si on est sur un bord
	 * 					<br>- (0, 0) si on est a l'interieur
	 * 
	 </font>*/
	public Couple KarushKuhnTucker(Couple P) {
		Couple mu = null;
		
		Couple gradientCurrentPoint = problemeTraite.gradientf(P);
		
		if(this.domaine.estCoin(P)!=null){// cas 1: sur coin
			
			// get the two constraints on this point
			Contrainte[] coin = this.domaine.estCoin(P);
			
			// we suppose there are only two constraints at this point
			if(coin.length!=2){
				System.out.println("Error! we don't have exactly two constraintes");
			}
			
			// get the gradient of the two constraints
			Couple gradientContrainte1 = coin[0].getGradient();
			Couple gradientContrainte2 = coin[1].getGradient();
			// find the decomposition
			Couple ab = Couple.decompose(gradientCurrentPoint, gradientContrainte1, gradientContrainte2);
			if(ab.x<0 && ab.y<0){
				//// if the two decompositions are both less than 0, KKT satisfied
				//// or else KKT is not satisfied
				mu = ab;
			}
		}else if(this.domaine.estSurBord(P)!=null){// cas 2: sur Bord
			
			// get the constraint, its unitary board and its gradient on this point
			Contrainte c = this.domaine.estSurBord(P);
			Couple cBordUni = c.getVecteurUnitaireBord();
			Couple gradientContrainte = c.getGradient();
			
			if(gradientCurrentPoint.estPerpendiculaire(cBordUni)){
				//// if the gradient of the current point is parallel to the constraint, KKT satisfied
				//// or else KKT is not satisfied
				double rt = -gradientCurrentPoint.norme()/gradientContrainte.norme();
				mu = new Couple(rt, 0);
			}
		}else{
			if(estNul(gradientCurrentPoint.norme())){// cas 3: a l'interieur
				
				//// if the norm of the gradient of the current point is equal to 0, KKT satisfied
				//// or else KKT is not satisfied
				mu = new Couple(0,0);
			}
		}
		return mu;	
	}
	
	/**
	 * <br>Recherche la prochaine direction a suivre.
	 * La methode considere les cas ou :
	 *    		<br>pointCourant est a  l'interieur du domaine, 
	 *    		<br>pointCourant est sur un bord, 
	 *    		<br>pointCourant est sur un coin 
	 *<br> Si la descente est finie, la methode passe this.finie a true.
	 </font>*/
	public void prochaineDirection() {
		Contrainte c;
		Contrainte [] coin;
		c = this.domaine.estSurBord(pointCourant);
		if (c != null) {
			coin = this.domaine.estCoin(pointCourant);
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
	 * Rien a  modifier ; connaissant le point courant et la direction a  suivre, 
	 * la methode recherche le point courant suivant. 		
	 * @return  
	 * Si pointCourant est le point courant (qui peut etre a l'interieur de domaine, sur un 
	 * bord du domaine ou sur un coin) et si d est la direction a suivre, la methode retourne
	 *   <br>- soit le point courant suivant (qui peut etre a l'interieur du domaine, sur un bord, sur un coin)
	 *   <br>- soit null si elle a mis en evidence que la fonction f n'atteint pas de minimum 
	 * 		   sur le domaine considere.
	 */
	public Couple pointSuivantDescente() {
		double t1;
		/*
		 * Explications concernant l'instruction suivante.
		 * On considere une demi-droite parametree par  t -> this.pointCourant + t * this.direction ; 
		 * cette demi-droite peut partir de l'interieur du domaine, ou d'un bord, ou d'un coin 
		 * et peut longer un bord du domaine.
		 * Si la demi-droite est toute entiere dans le domaine, ce qui n'est possible que 
		 * si le domaine n'est pas borne,l'instruction retourne -1. 
		 * Sinon, elle retourne une valeur de t > 0 pour laquelle pointCourant + t * direction appartient 
		 * a un bord du domaine.		
		 */
		t1 = domaine.intersection(this.pointCourant, this.directionASuivre);
		// Si la demi-droite rencontre un bord du domaine et si la derivee de t -> pointCourant + t * direction
		// est positive en ce point d'intersection, on retourne ce point.
		if ((t1 > 0) && (problemeTraite.phiDerivee(this.pointCourant, this.directionASuivre, t1) <= 0)) 
			return this.pointCourant.ajoute(this.directionASuivre.mult(t1));
		
		// Si la demi-droite est toute entiere dans le domaine, on cherche un point ou la derivee de
		// t -> pointCourant + t * direction soit positive
		if (t1 < 0) {
			t1 = chercheSecondPoint(pointCourant, directionASuivre);
			// Dans le cas ci-dessous, le probleme n'atteint pas de minimum
			if (t1 < 0) return null;
			// sinon la derivee de t -> pointCourant + t * direction est positive pour t = t1
		}
		return dichotomie(pointCourant, directionASuivre, t1);
	}

	/**
	 * Permet de connaitre le probleme traite.
	 * @return le probleme traite
	 */
	public Pb getPb() {
		return this.problemeTraite;
	}

	/**
	 * Permet de connaitre le point courant de la methode de descente.
	 * @return le point courant
	 */
	public Couple getP() {
		return this.pointCourant;
	}

	/**
	 * Permet de connaitre la  direction a  suivre par la methode de descente a  partir du point courant pointCourant
	 * @return la direction a  suivre.
	 */
	public Couple getDirection() {
		return this.directionASuivre;
	}

	/** 
	 * permet d'initialiser le point de depart de la methode de descente
	 * @param p la valeur a  donner a  pointCourant.
	 */
	public void setP(Couple p) {
		this.pointCourant = p;
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
	 * @return la valeur de la variable booleenne stoppe
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
	 * Permet de savoir si la methode de descente est terminee
	 * @return true si la descente est terminee et false sinon
	 */
	public boolean isFinie() {
		return this.finie;
	}

	/**
	 * Apres que la descente soit terminee, sert a  savoir si le probleme traite
	 * atteint sa borne inferieure ou non
	 * @return true ou false selon que le probleme traite atteint sa borne inferieure ou non
	 */
	public boolean atteintSonMinimum() {
		return this.atteintMinimum;
	}

	/** 
	 * Permet de preciser le test d'arret de la methode de descente 
	 * @param seuil si la norme du gradient est inferieur a  seuil, la methode de descente s'arrete
	 */
	public void setSeuil(double seuil) {
		this.seuil = seuil;
	}

	/**
	 * Permet de connaitre le nombre de pas effectues depuis le debut de la methode de descente
	 * @return le nombre de pas effectues depuis le debut de la methode de descente
	 */
	public int getNbPas() {
		return this.nbPas;
	}

	/**
	 * Permet de connaitre le temps d'attente entre deux pas de la methode de descente
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