package de.fhg.iais.roberta.visitor.hardware.actor;

import de.fhg.iais.roberta.syntax.action.sound.PlayNoteAction;
import de.fhg.iais.roberta.syntax.action.sound.ToneAction;
import de.fhg.iais.roberta.visitor.hardware.IHardwareVisitor;

public interface ISimpleSoundVisitor<V> extends IHardwareVisitor<V> {

    /**
     * visit a {@link ToneAction}.
     *
     * @param toneAction to be visited
     */
    V visitToneAction(ToneAction toneAction);

    /**
     * visit a {@link PlayNoteAction}.
     *
     * @param playNoteAction
     */
    V visitPlayNoteAction(PlayNoteAction playNoteAction);
}