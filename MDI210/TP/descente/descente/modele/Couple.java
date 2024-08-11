package descente.modele;


/**
 * Modelise un couple de deux double(s) ; peut modeliser un point ou un vecteur du plan reel
 *
 */
public class Couple {
	public double x, y;
	
	public Couple() {
		super();
	}

	public Couple(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Ajoute un couple au couple concerne (this.x, this.y) (sans modifier celui-ci) et retourne le resultat
	 * @param v le couple a ajouter
	 * @return le resultat de l'addition
	 */
	public Couple ajoute(Couple v) {
		return new Couple(this.x + v.x, this.y + v.y);
	}
	
	/**
	 * Multiplie le couple concerne (this.x, this.y) par un double (sans modifier le couple concerne) et retourne le resultat
	 * @param t le multiplicateur
	 * @return le couple qui resulte de la multiplication
	 */
	public Couple mult(double t) {
		return new Couple(t * this.x, t * this.y);
	}
	
	/**
	 * Soustrait un couple du couple concerne (sans modifier celui-ci) et retourne le resultat
	 * @param v le couple a soustraire
	 * @return le resultat de la soustraction
	 */
	public Couple soustrait(Couple v) {
		return this.ajoute(v.mult(-1));
	}
	
	/**
	 * Effectue le produit scalaire du couple concerne avec un autre couple
	 * @param v le couple avec lequel le produit scalaire est effectue
	 * @return le resultat du produit scalaire
	 * 
	 */
	public  double produitScalaire(Couple v) {
		return this.x * v.x + this.y * v.y;
	}
	
	/**
	 * @return 
	 * - si le couple modelise un vecteur, calcule la norme du vecteur concerne (this.x, this.y)
	 * <br>- si le couple modelise un point, calcule la distance de ce point a l'origine
	 */
	public double norme() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}
	
	/**
	 * Lorsque le couple modelise un point, calcule la distance du point concerne (this.x, this.y) a un autre point P
	 * @param P le point pour lequel on veut calculer la distance
	 * @return la distance du point concerne au point P
	 */
	public double distance(Couple P) {
		return this.soustrait(P).norme();	
	}
	
	/**
	 * Lorsque le couple (this.x, this.y) modelise un vecteur, indique si ce vecteur est perpendiculaire a un autre vecteur 
	 * @param v le vecteur compare au vecteur concerne
	 * @return true si v est perpendiculaire au vecteur concerne et false sinon
	 */
	public boolean estPerpendiculaire(Couple v) {
		return Descente.estNul(this.produitScalaire(v));
	}
	
	/**
	 * decompose un vecteur selon une base de deux vecteurs
	 * @param v le vecteur a decomposer
	 * @param v1 le premier vecteur de la base
	 * @param v2 le second vecteur de la base
	 * @return les deux composantes de v dans la base (v1, v2) ou bien null si v1 et v2 sont paralleles
	 */
	public static Couple decompose(Couple v, Couple v1, Couple v2) {

		double determinant = v1.x * v2.y - v1.y * v2.x;
		
		if (Descente.estNul(determinant)) return null;
		
		double mu1, mu2;
		mu1 = (v.x * v2.y - v.y * v2.x) / determinant;
		mu2 = (v.y * v1.x - v.x * v1.y) / determinant;
			
		return new Couple(mu1, mu2);
	}
	
	@Override
	/**
	 * teste l'egalite d'un couple avec un autre
	 */
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj == null) return false;
		Couple c = (Couple)obj;
		return Descente.estNul(this.x - c.x) && Descente.estNul(this.y - c.y);
	}
	
	/**
	 * tronque un double en imposant le nombre de chiffres apres la virgule
	 * @param val le double a tronquer, qui ne sera pas modifie
	 * @param nb le nombre de chiffres apres la virgule
	 * @return le double tronque
	 */
	public static double tronquer(double val, int nb) {
		double puissance = 1;
		for (int i = 0; i < nb; i++) puissance *= 10;
		return Math.round(puissance * val) /puissance;
	}

	/** 
	 * tronque les deux composantes d'un couple (le couple concerne n'est pas modifie) et retourne le resultat
	 * @param nb le nombre de chiffres derriere la virgule
	 * @return le couple tronque
	 */
	
	public Couple tronquer(int nb) {
		return new Couple(Couple.tronquer(this.x, nb), Couple.tronquer(this.y, nb));	
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}	
	
}
