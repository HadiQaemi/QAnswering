package org.neuroph.nnet.comp;

import org.neuroph.core.Neuron;
import org.neuroph.core.input.WeightedSum;
import org.neuroph.core.transfer.Linear;

/**
 *
 * @author zoran
 */
public class InputNeuron extends Neuron {
	private static final long serialVersionUID = 1L;


	public InputNeuron() {
            super(new WeightedSum(), new Linear());
        }


    @Override
    public void calculate() {
        this.output = this.netInput;
    }

}
