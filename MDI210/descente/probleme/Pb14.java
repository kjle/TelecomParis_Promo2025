package probleme;

import java.util.ArrayList;

import descente.modele.Contrainte;
import descente.modele.Couple;
import descente.modele.Domaine;


public class Pb14 extends Pb {
	public Pb14() {
		ArrayList<Contrainte> contraintes = new ArrayList<Contrainte>();
		contraintes.add(new Contrainte(-1, 1, 0));
		contraintes.add(new Contrainte(-1, 1, 3));
		//contraintes.add(new Constraint(1, 1, -4));
		domaine = new Domaine(contraintes, true);
	}

	public double f(Couple P) {
		return Math.exp(P.x + P.y) + P.x * P.x + 2 * P.y * P.y;
	}
	
	public Couple gradientf(Couple P) {
		double valx, valy;
		
		valx = Math.exp(P.x + P.y) + 2* P.x;
		valy = Math.exp(P.x + P.y) + 4* P.y;
		return new Couple(valx, valy);
	}
	
	public String toString() {
		return "f = exp(x + y) + x^2 + 2 y^2\n" + super.toString();
	}
}
