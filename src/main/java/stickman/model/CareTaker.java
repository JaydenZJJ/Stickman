package stickman.model;

public class CareTaker {
	private Memento savedMemento;

	public void add(Memento memento){
		savedMemento = memento;
	}

	public Memento get(){
		if (savedMemento == null){
			return null;
		}
		return savedMemento.getNewInstance();
	}
}
