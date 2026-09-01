import com.example.data.repository.normalizeChapterName
import com.example.data.repository.normalizeSubjectName

fun main() {
    val sub = normalizeSubjectName("General English")
    val chap = normalizeChapterName("cloze test", sub)
    println("sub: $sub, chap: $chap")
}
