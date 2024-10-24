package probleme;

import java.util.ArrayList;

import descente.modele.Contrainte;
import descente.modele.Couple;
import descente.modele.Domaine;


public class Pb13 extends Pb {
	public Pb13() {
		ArrayList<Contrainte> contraintes = new ArrayList<Contrainte>();
		contraintes.add(new Contrainte(0, -1, 0));
		contraintes.add(new Contrainte(-1, 0, -1));
		contraintes.add(new Contrainte(1, 0, -1));
		domaine = new Domaine(contraintes, true);
	}

	public double f(Couple P) {
		return 2 * P.x*P.x*P.x*P.x + P.y*P.y;
	}

	public Couple gradientf(Couple P) {
		double valx, valy;

		if (P == null) return null;
		valx = 8 * P.x*P.x*P.x ;
		valy = 2 * P.y;
		return new Couple(valx, valy);
	}
	
	public String toString() {
		String chaine = "f = 2 x^4 + y^2\n" +super.toString();
		return chaine;
	}
}
