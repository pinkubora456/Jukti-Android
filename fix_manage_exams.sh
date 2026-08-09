awk '
/^@Composable$/ {
    if (prev_composable) {
        # skip this duplicate
    } else {
        prev_composable = 1
        print
    }
    next
}
{
    prev_composable = 0
    print
}
' app/src/main/java/com/example/ui/screens/ManageExamsScreen.kt > tmp_me.kt
mv tmp_me.kt app/src/main/java/com/example/ui/screens/ManageExamsScreen.kt
