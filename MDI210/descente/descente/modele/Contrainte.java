package descente.modele;

/**
 * Modelise une contrainte affine qui s'ecrit : coeffx * x + coeffy * y + constante <= 0 .
 * La droite d'equation : coeffx * x + coeffy * y + constante = 0 est la droite "frontiere".
 */
public class Contrainte {
	private final double coeffx;
	private final double coeffy;
	private final double constante;
	private Couple gradient;     // le gradient de la fonction : (x, y) -> coeffx * x + coeffy * y + constante
	private Couple vecteurUnitaireBord; // un vecteur unitaire parallele a la droite frontiere

	/**
	 * @param coeffx le coefficient de x
	 * @param coeffy le coefficient de y
	 * @param constante la constante
	 */
	public Contrainte(double coeffx, double coeffy, double constante) {
		super();
		this.coeffx = coeffx;
		this.coeffy = coeffy;
		this.constante = constante;
		this.gradient = new Couple(coeffx, coeffy);
		this.vecteurUnitaireBord = new Couple(-coeffy, coeffx);
		this.vecteurUnitaireBord = this.vecteurUnitaireBord.mult(1/this.vecteurUnitaireBord.norme());
	}
	
	/**
	 * Calcule la valeur de coeffx * x + coeffy * y + constante pour un couple de reel donne
	 * @param P le point en lequel on calcule la valeur de la fonction affine
	 * @return le resultat du calcul
	 */
	public double valeur(Couple P) {
		return this.coeffx * P.x + this.coeffy * P.y + this.constante;
	}

	/**
	 * Recherche si le point P verifie ou non la contrainte
	 * @param P 	le point considere
	 * @return 		true si le point verifie la contrainte, false sinon
	 */
	public boolean estVerifie(Couple P) {
		double val = this.valeur(P);
		return  val <= 0 || Descente.estNul(val);	
	}

	/**
	 * Recherche si le point P sature ou non la contrainte, c'est-a-dire appartient a la droite frontiere
	 * @param P 	le point considere
	 * @return 		true si le point appartient a la droite frontiere, false sinon
	 */
	public boolean estSature(Couple P) {
		return Descente.estNul(valeur(P));	
	}

	/**
	 * On cherche le point d'intersection de la droite frontiere avec la demi-droite parametree pour t > 0 par : 
	 *  t -> P0 + td 
	 * @param P0 l'origine de la demi-droite avec laquelle on cherche l'intersection
	 * @param d le vecteur directeur de la demi-droite
	 * @return s'il y a intersection, la valeur du parametre t correspondant a l'intersection, 
	 *         sinon -1 
	 */
	public double intersection(Couple P0, Couple d) {
		double t;
		double produit = d.produitScalaire(gradient);

		if (Descente.estNul(produit)) return -1;
		t = (-constante - P0.produitScalaire(gradient)) / produit;
		return t;	
	}

	/**
	 * Calcule l'intersection de la droite frontiere de la contrainte concernee 
	 * avec la droite frontiere d'une autre contraintes
	 * @param c la contrainte pour laquelle on calcule l'intersection
	 * @return le point d'intersection des deux droites, et null si les droites sont paralleles
	 */
	public Couple intersection(Contrainte c) {
		double determinant = this.coeffx * c.coeffy - this.coeffy * c.coeffx;

		if (Descente.estNul(determinant)) return null;

		Couple I = new Couple();
		I.x = (c.constante * this.coeffy - this.constante * c.coeffy) / determinant;
		I.y = (this.constante * c.coeffx - c.constante * this.coeffx) / determinant;

		return I;	
	}

	/**
	 * Regarde si c est parallele a this.
	 * Si c'est le cas, regarde si les vecteurs gradients sont dans le eme sens ; alors l'une des deux 
	 * contraintes contient tout le domaine, on peut éliminer celle-ci. 
	 * Si les vecteurs gradients sont en sens opposés, soit les les deux demi-plans
	 * correspondant sont d'intersection vide, le domain est vide, soit les deux contraintes 
	 * donnent des bords au domaine.
	 * @param c	la contrainte comparee a this
	 * @return 	0 si la contrainte c n'est pas parallele a this
	 * 			<br> -1 si la contrainte c est parallele a this, que leurs vecteurs gradients sont dans le meme sens 
	 * 				  et que c rend this inutile
	 * 			<br> 1 si la contrainte c est parallele a this  que leurs vecteurs gradients sont dans le meme sens 
	 *               et que this rend c inutile
	 * 			<br> -2 si la contrainte c est parallele a this, que leurs vecteurs gradients sont en sens opposes
	 *                et que les deux demi-plans correspondants aux contraintes sont d'intersection vide
	 * 			<br> 2 si la contrainte c est paralleles a this, que leurs vecteurs gradients sont en sens opposes
	 *                et que c et this délimitent le domaine
	 */
	public int estParallele(Contrainte c) {
		Couple g = this.getGradient();
		Couple g1 = c.getGradient();

		if (!g.estPerpendiculaire(c.getVecteurUnitaireBord())) return 0;
		double produit = g.produitScalaire(g1);
		if (produit > 0) 
			if (this.estVerifie(c.unPointFrontiere())) return -1;
			else return 1;
		else 
			if (this.estVerifie(c.unPointFrontiere())) return 2;
			else return -2;
	}

	/**
	 * Cherche un point de la droite frontiere
	 * @return un point de la droite frontiere
	 */
	public Couple unPointFrontiere() {
		if (this.coeffy != 0) return new Couple(0, -this.constante / this.coeffy);
		else return new Couple(-this.constante / this.coeffx, 0);
	}

	@Override
	public String toString() {
		String chaine;
		boolean signe = true;
		if (this.coeffx == (int)this.coeffx)
			if (this.coeffx == 0) {
				chaine = "      ";
				signe = false;
			}
			else if (this.coeffx == 1) chaine = "   x  ";
			else if (this.coeffx == -1) chaine = "  -x  ";
			else chaine =  (int)this.coeffx + " x ";
		else 
			chaine = this.coeffx + " x  ";

		if (this.coeffy == (int)this.coeffy) 
			if (this.coeffy == 0) chaine += "       <=  ";
			else if (this.coeffy == 1) 
				if (signe) chaine += " +   y  <=  ";
				else chaine += "      y >=  ";
			else if (this.coeffy == -1) 
				if (signe) chaine += " -   y  <=  ";
				else chaine += "   - y <=  ";
			else if (this.coeffy < 0) 
				if (signe) chaine += " - " + (-(int)this.coeffy) +  " y  <=  ";
				else chaine += "     " + ((int)this.coeffy) +  " y  <=  ";
			else chaine += " + " + (int)this.coeffy +  " y  <=  ";
		else 	
			if (this.coeffy > 0) 
				if (signe) chaine += "+ " + this.coeffy + " y <= ";
				else chaine += " " + this.coeffy + " y <= ";
			else if (this.coeffy > 0) chaine += "         <= ";
			else chaine += "- " + (-this.coeffy) + " y <= ";	

		if (this.constante == (int)this.constante) chaine += -(int)this.constante;
		else chaine += -this.constante;
		return chaine;
	}

	/**
	 * @return un vecteur unitaire de la droite frontiere
	 */
	public Couple getVecteurUnitaireBord() {
		return this.vecteurUnitaireBord;
	}

	/**
	 * @return le gradient de la fonction : (x, y) -> coeffx * x + coeffy * y + constante
	 */
	public Couple getGradient() {
		return this.gradient;
	}

	/**
	 * @return le coefficient de x dans l'equation : (x, y) -> coeffx * x + coeffy * y + constante 
	 * donnant la droite frontiere
	 */
	public double getCoeffx() {
		return this.coeffx;
	}
	
	/**
	 * @return le coefficient de y dans l'equation : (x, y) -> coeffx * x + coeffy * y + constante 
	 * donnant la droite frontiere
	 */
	public double getCoeffy() {
		return this.coeffy;
	}

	/**
	 * @return la constante dans l'equation : (x, y) -> coeffx * x + coeffy * y + constante 
	 * donnant la droite frontiere
	 */
	public double getConstante() {
		return this.constante;
	}
}
