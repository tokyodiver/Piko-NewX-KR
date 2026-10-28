package app.crimera.patches.newx.misc.dynamiccolor

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.util.getReference
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.registersUsed
import app.morphe.patcher.util.smali.toInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.HiddenApiRestriction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MethodImplementationBuilder
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableAnnotation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// Prevents an argument-slot error from recoloring native text or surfaces.
class NativeAccentHookTest {
    @Test
    fun `only primary onPrimary and link arguments are rewritten in each scheme`() {
        for ((isLight, startRegister) in listOf(true, false).flatMap { light ->
            listOf(4, 5, 6, 20).map { start -> light to start }
        }) {
            val body = MethodImplementationBuilder(56).apply {
                addInstruction("return-void".toInstruction())
            }
            val method = MutableMethod(ImmutableMethod(
                "Ltest/NativePalette;", "factory", emptyList<ImmutableMethodParameter>(), "V",
                AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
                emptySet<ImmutableAnnotation>(), emptySet<HiddenApiRestriction>(), body.methodImplementation,
            ))
            val original = method.instructions.single()
            method.injectNativeAccentColors(0, startRegister, isLight)
            val instructions = method.instructions
            val brightness = if (isLight) "Light" else "Dark"
            val expectedRegisters = listOf(2, 10, 12).map { it + startRegister }
            val expectedRoles = listOf("Accent", "OnAccent", "Accent")
            expectedRegisters.forEachIndexed { index, register ->
                val call = instructions[index * 2]
                assertEquals(listOf(register, register + 1), call.registersUsed)
                assertEquals("native$brightness${expectedRoles[index]}", call.getReference<MethodReference>()?.name)
                assertEquals(Opcode.MOVE_RESULT_WIDE, instructions[index * 2 + 1].opcode)
                assertEquals(register, (instructions[index * 2 + 1] as OneRegisterInstruction).registerA)
            }
            assertEquals(7, instructions.size)
            assertSame(original, instructions.last())
        }
    }
}
