package probleme;

import java.util.ArrayList;

import descente.modele.Contrainte;
import descente.modele.Couple;
import descente.modele.Domaine;


public class Pb15 extends Pb {
	public Pb15() {
		ArrayList<Contrainte> contraintes = new ArrayList<Contrainte>();
		contraintes.add(new Contrainte(-1, 0, -1));
		contraintes.add(new Contrainte(0, -1, -1));
		contraintes.add(new Contrainte(1, 0, -1));
		contraintes.add(new Contrainte(0, 1, -1));
		domaine = new Domaine(contraintes, true);
	}

	public double f(Couple P) {
		return P.x * P.x +  2 *P.y * P.y;
	}
	
	public Couple gradientf(Couple P) {
		double valx, valy;
		
		valx = 2 * P.x;
		valy = 4 * P.y;
		return new Couple(valx, valy);
	}
	
	public String toString() {
		return "f =  x^2 + 2 y^2\n" + super.toString();
	}
}
