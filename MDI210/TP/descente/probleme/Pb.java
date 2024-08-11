package probleme;

import descente.modele.Contrainte;
import descente.modele.Couple;
import descente.modele.Domaine;

/**
 * Modelise un probleme de minimisation d'une fonction sur un domaine limite par des contraintes affines
 */
public abstract class Pb {
	public Domaine domaine;

	/**
	 * La fonction à optimiser
	 * @param P		le couple  (x, y) 
	 * @return		la valeur de la fonction
	 */
	public abstract double f(Couple P);

	/**
	 * Le gradient de la fonction a minimiser
	 * @param P le point ou est calcule le gradient
	 * @return une variable de type Couple contenant le gradient de f au point P
	 */
	public abstract Couple gradientf(Couple P);

	/**
	 * Si t-> P0 + td est l'equation parametrique d'une demi-droite, 
	 * calcule la derivee de la fonction t -> f(P0 + td)
	 * @param P0 l'origine de la demi-droite
	 * @param d la direction de la demi-droite
	 * @param t la valeur du parametre pour lequel on calcule la derivee
	 * @return le resultat du calcul
	 */
	public double phiDerivee(Couple P0,  Couple d, double t) {
		Couple P = P0.ajoute(d.mult(t));
		return d.produitScalaire(gradientf(P));
	}
	
	public String toString() {
		String chaine ;
		if (this.domaine.getContraintes().size() != 0 ) {
			chaine = "avec :\n";
			for (Contrainte c : this.domaine.getContraintes()) 
				chaine += c + "\n";
		}
		else chaine = "Pas de contraintes";
		return chaine;
	}
	
	/**
	 * @return le domaine du probleme
	 */
	public Domaine getDomaine() {
		return domaine;
	}

}