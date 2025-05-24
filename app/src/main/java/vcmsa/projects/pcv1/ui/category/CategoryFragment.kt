package vcmsa.projects.pcv1.ui.category

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.Category
import vcmsa.projects.pcv1.data.CategoryRepository
import vcmsa.projects.pcv1.databinding.FragmentCategoryBinding
import vcmsa.projects.pcv1.databinding.DialogAddCategoryBinding
import vcmsa.projects.pcv1.util.SessionManager
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CategoryAdapter
    private lateinit var repository: CategoryRepository
    private lateinit var session: SessionManager
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        currentUserId = session.getUserId()
        repository = CategoryRepository(AppDatabase.getInstance(requireContext()).categoryDao())

        adapter = CategoryAdapter(
            onEdit = { category -> showEditCategoryDialog(category) },
            onDelete = { category ->
                lifecycleScope.launch {
                    repository.deleteCategory(category)
                    loadCategories()
                }
            }
        )

        binding.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategories.adapter = adapter

        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        loadCategories()

        return binding.root
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = repository.getCategoriesForUser(currentUserId)
            adapter.submitList(categories)
        }
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
            .setTitle("Add Category")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val name = dialogBinding.editCategoryName.text.toString().trim()
                val icon = dialogBinding.editCategoryIcon.text.toString().trim()
                if (name.isNotEmpty()) {
                    val category = Category(userId = currentUserId, name = name, icon = icon)
                    lifecycleScope.launch {
                        repository.addCategory(category)
                        loadCategories()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditCategoryDialog(category: Category) {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater).apply {
            editCategoryName.setText(category.name)
            editCategoryIcon.setText(category.icon)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Category")
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { _, _ ->
                val newName = dialogBinding.editCategoryName.text.toString().trim()
                val newIcon = dialogBinding.editCategoryIcon.text.toString().trim()
                if (newName.isNotEmpty()) {
                    val updated = category.copy(name = newName, icon = newIcon)
                    lifecycleScope.launch {
                        repository.updateCategory(updated)
                        loadCategories()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
