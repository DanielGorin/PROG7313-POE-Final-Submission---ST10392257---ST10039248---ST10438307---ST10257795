package vcmsa.projects.pcv1.ui.category

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import vcmsa.projects.pcv1.data.AppDatabase
import vcmsa.projects.pcv1.data.Category
import vcmsa.projects.pcv1.data.CategoryRepository
import vcmsa.projects.pcv1.databinding.DialogAddCategoryBinding
import vcmsa.projects.pcv1.databinding.FragmentCategoryBinding
import vcmsa.projects.pcv1.util.SessionManager

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
            },
            onClick = { category ->
                // Future use: navigate to expenses for this category
                // For now, you could show a Toast or Log
                Toast.makeText(requireContext(), "Selected: ${category.name}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategories.adapter = adapter

        // Attach swipe-to-delete logic
        attachSwipeToDelete()

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

    private fun attachSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val category = adapter.getItemAt(position)

                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete '${category.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            repository.deleteCategory(category)
                            loadCategories()
                            Snackbar.make(binding.root, "Category deleted", Snackbar.LENGTH_LONG)
                                .setAction("Undo") {
                                    lifecycleScope.launch {
                                        repository.addCategory(category)
                                        loadCategories()
                                    }
                                }
                                .show()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .setCancelable(false)
                    .show()
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerCategories)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
