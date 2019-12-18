/**
 * Development Notes
 *
 * 1. Add the correct plugin interfaces (e.g. `TerraformValidateCommandPlugin`).
 * 2. Add the correct method if not arleady present.  If you do, ensure that you
 *    call `detectVersion()` at the beginning of the method.
 * 3. Introduce logic to the method that utilizes the `Comparable` class,
 *   `SemanticVersion` to determine what behavior.  The original break with
 *   the removal of the `-check-variables=false` in the
 *   `apply(TerraformValidateCommand)` provides a working example.
 * 4. If you introduced a new plugin interface, ensure that you add the
 *   the plugin class to the appropriate stage or command's `DEFAULT_PLUGINS`
 *   list.  See `TerraformValidateCommand` for an example.
 * 5. Finally, update the tests in `TerraformPluginTests` to cover the new
 *   behavior.
 *
 * See apply(TerraformValidateCommand) for an over-documented example
 * before strating on your own.
 */
class TerraformPlugin implements TerraformValidateCommandPlugin {

    static SemanticVersion version
    static final String DEFAULT_VERSION = '0.11.0'

    static SemanticVersion detectVersion() {
        if(version == null) {
            def jf = Jenkinsfile.instance.original
            if (jf.fileExists('.terraform-version')) {
                version = new SemanticVersion((jf.readFile('.terraform-version') as String).trim())
            } else {
                version = new SemanticVersion(DEFAULT_VERSION)
            }
        }
    }

    static void withVersion(String version) {
        this.version = new SemanticVersion(version)
    }

    @Override
    void apply(TerraformValidateCommand command) {
        // This must be at the top of every apply() call.  This is
        // the best way to ensure that Jenkinsfile.instance.original is
        // properly set so that we can detect the terraform-version.
        detectVersion()

        // Due to CPS shenanigans you can't use Groovy magic and do
        // version < new SemanticVersion('1.2.3').  This becomes
        // if(-1) or if(0) or if(1) resulting in truthy values for
        // both less than and greater than and false-ey values when
        // equal.

        // If < 0.12.0 add -check-variables=false
        if(version.compareTo(new SemanticVersion('0.12.0')) < 0) {
            command.withArgument('-check-variables=false')
        }
    }
}