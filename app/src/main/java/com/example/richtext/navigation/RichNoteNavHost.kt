package com.example.richtext.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.richtext.core.markdown.MarkdownRenderer
import com.example.richtext.feature.detail.NoteDetailRoute
import com.example.richtext.feature.editor.NoteEditorRoute
import com.example.richtext.feature.list.NoteListRoute
import com.example.richtext.feature.settings.RecycleBinRoute
import com.example.richtext.feature.settings.SettingsRoute

object Routes {
    const val NOTE_LIST = "note_list"
    const val SETTINGS = "settings"
    const val RECYCLE_BIN = "recycle_bin"

    fun noteDetail(noteId: String) = "note_detail/$noteId"
    fun noteEditor(noteId: String) = "note_editor/$noteId"
}

@Composable
fun RichNoteNavHost(
    markdownRenderer: MarkdownRenderer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.NOTE_LIST,
        modifier = modifier,
    ) {
        composable(Routes.NOTE_LIST) {
            NoteListRoute(
                onOpenNote = { id -> navController.navigate(Routes.noteDetail(id)) },
                onCreateNote = { navController.navigate(Routes.noteEditor("new")) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenRecycleBin = { navController.navigate(Routes.RECYCLE_BIN) },
            )
        }

        composable(
            route = "note_detail/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType }),
        ) { entry ->
            NoteDetailRoute(
                onBack = { navController.popBackStack() },
                onEdit = {
                    val noteId = entry.arguments?.getString("noteId").orEmpty()
                    navController.navigate(Routes.noteEditor(noteId))
                },
                markdownRenderer = markdownRenderer,
            )
        }

        composable(
            route = "note_editor/{noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    defaultValue = "new"
                },
            ),
        ) { entry ->
            NoteEditorRoute(
                onBack = { navController.popBackStack() },
                markdownRenderer = markdownRenderer,
            )
        }

        composable(Routes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }

        composable(Routes.RECYCLE_BIN) {
            RecycleBinRoute(onBack = { navController.popBackStack() })
        }
    }
}
