package app.crimera.patches.newx.misc.composer

import app.crimera.patches.common.requireExactlyOne
import app.crimera.patches.common.scopedMatchAll
import app.crimera.patches.common.scopedMatchAllOrNull
import app.crimera.patches.newx.settings.Categories
import app.crimera.patches.newx.settings.newXSettings
import app.crimera.patches.newx.utils.unsupportedOnMount
import app.crimera.patches.settings.group
import app.crimera.patches.settings.returnVoidIfEnabled
import app.crimera.patches.settings.settingStrings
import app.crimera.patches.settings.toggle
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.literal
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resource.resourceId
import app.morphe.patcher.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val COMPOSER = "Landroidx/compose/runtime/Composer;"
private const val MODIFIER = "Landroidx/compose/ui/Modifier;"
private const val CALLBACK = "Lkotlin/jvm/functions/Function1;"

private fun Method.calls(reference: MethodReference): Boolean =
    implementation?.instructions?.any {
        it.getReference<MethodReference>()?.toString() == reference.toString()
    } == true

private object AudienceLabel : Fingerprint(
    definingClass = "Lcom/x/composer/narrowcast/",
    returnType = "Ljava/lang/String;",
    filters = listOf(literal({ resourceId(ResourceType.STRING, "narrowcast_default_audience") })),
    custom = { method, _ ->
        AccessFlags.STATIC.isSet(method.accessFlags) &&
            method.parameterTypes.size == 2 && method.parameterTypes.last() == COMPOSER
    },
)

@Suppress("unused")
val hideAudienceSelectorPatch = bytecodePatch(
    name = "NewX: Hide audience selector",
    description = "Adds a default-on option to hide the audience selector beside the composer avatar.",
) {
    unsupportedOnMount()
    // First validated against the supplied production APKM; do not claim untested older ABIs.
    compatibleWith(Compatibility(
        name = "NewX",
        packageName = "com.twitter.android",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "12.37.2-prod.01")),
    ))

    val hide = newXSettings {
        category(Categories.APPEARANCE) {
            group(
                id = "newx.appearance.composer",
                strings = settingStrings("piko_newx_composer"),
                iconResourceName = "ic_vector_text_size",
                order = 300,
            ) {
                toggle(
                    id = "newx.appearance.composer.hide_audience",
                    strings = settingStrings("piko_newx_hide_audience"),
                    defaultValue = true,
                    order = 100,
                    rebootApp = true,
                )
            }
        }
    }

    execute {
        val label = requireExactlyOne("NewX public-audience label", AudienceLabel.scopedMatchAll()).method
        val model = label.parameterTypes.first().toString()
        // R8 merges this label+chevron lambda into an unrelated package. Follow its label call,
        // not the merged owner's obfuscated identity or switch-case ordinal.
        val content = requireExactlyOne(
            "NewX audience button content lambda",
            Fingerprint(
                name = "invoke",
                returnType = "Ljava/lang/Object;",
                custom = { method, _ -> method.calls(label) },
            ).scopedMatchAllOrNull().orEmpty(),
        ).originalMethod.definingClass
        val renderer = requireExactlyOne(
            "NewX isolated audience button renderer",
            Fingerprint(
                definingClass = "Lcom/x/composer/ui/",
                returnType = "V",
                custom = { method, _ ->
                    val parameters = method.parameterTypes.map(CharSequence::toString)
                    AccessFlags.STATIC.isSet(method.accessFlags) &&
                        listOf(model, COMPOSER, MODIFIER, CALLBACK, "Z").all { parameters.count { p -> p == it } == 1 } &&
                        parameters.all { it in setOf(model, COMPOSER, MODIFIER, CALLBACK, "Z", "I") } &&
                        method.implementation?.instructions?.any {
                            it.opcode == Opcode.NEW_INSTANCE && it.getReference<TypeReference>()?.type == content
                        } == true
                },
            ).scopedMatchAllOrNull().orEmpty(),
        ).method
        // Exit before the Compose group starts; no shared text resources or audience state change.
        hide.returnVoidIfEnabled(renderer, 0)
    }
}
