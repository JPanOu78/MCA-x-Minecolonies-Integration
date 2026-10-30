package com.minecolonies.core.entity.ai.workers.guard;

import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.core.entity.citizen.EntityCitizen;

/**
 * Compatibility shim for addon mods (such as MineColonies Mages) that reference
 * the legacy class name RangerCombatAI instead of RangeCombatAI.
 */
public class RangerCombatAI extends RangeCombatAI {

    public RangerCombatAI(EntityCitizen citizen, ITickRateStateMachine stateMachine, AbstractEntityAIGuard parentAI) {
        super(citizen, stateMachine, parentAI);
    }
}
