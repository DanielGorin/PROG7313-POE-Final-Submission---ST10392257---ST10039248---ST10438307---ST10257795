// Takudzwa Murwira – ST10392257, Jason Daniel Isaacs – ST10039248, Daniel Gorin – ST10438307 and Moegammad-Yaseen Salie – ST10257795
//PROG7313

//References:
//            https://medium.com/@SeanAT19/how-to-use-mpandroidchart-in-android-studio-c01a8150720f
//            https://chatgpt.com/
//            https://www.youtube.com/playlist?list=PLWz5rJ2EKKc8SmtMNw34wvYkqj45rV1d3
//            https://www.youtube.com/playlist?list=PLSrm9z4zp4mEPOfZNV9O-crOhoMa0G2-o
package vcmsa.projects.pcv1.ui.category

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

    // ViewBinding for this fragment's layout
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    // Adapter for RecyclerView to display categories
    private lateinit var adapter: CategoryAdapter

    // Repository for accessing Category data from the database
    private lateinit var repository: CategoryRepository

    // SessionManager to retrieve the current logged-in user's info
    private lateinit var session: SessionManager

    // Current user's ID to filter categories by user
    private var currentUserId: Int = -1

    // Called to create and return the fragment's view hierarchy
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)

        // Initialize session manager and get current user ID
        session = SessionManager(requireContext())
        currentUserId = session.getUserId()

        // Initialize repository with DAO from singleton database instance
        repository = CategoryRepository(AppDatabase.getInstance(requireContext()).categoryDao())

        // Initialize adapter with lambda callbacks for edit, delete, and item click actions
        adapter = CategoryAdapter(
            onEdit = { category -> showEditCategoryDialog(category) },  // Show dialog to edit category
            onDelete = { category ->
                lifecycleScope.launch {
                    repository.deleteCategory(category)  // Delete category from DB
                    loadCategories()                    // Refresh the list
                }
            },
            onClick = { category ->
                // Navigate to CategoryExpensesFragment with selected category ID as argument
                val action = CategoryFragmentDirections
                    .actionCategoryFragmentToCategoryExpensesFragment(category.id)
                findNavController().navigate(action)
                // Show a Toast message indicating selected category
                Toast.makeText(requireContext(), "Selected: ${category.name}", Toast.LENGTH_SHORT).show()
            }
        )

        // Set up RecyclerView with LinearLayoutManager and adapter
        binding.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategories.adapter = adapter

        // Attach swipe-to-delete gesture handling to RecyclerView
        attachSwipeToDelete()

        // Set click listener on FloatingActionButton to add a new category
        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        // Load and display categories for the current user
        loadCategories()

        return binding.root
    }

    // Loads categories from the repository filtered by the current user and submits them to the adapter
    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = repository.getCategoriesForUser(currentUserId)
            adapter.submitList(categories)
        }
    }

    // Displays a dialog to add a new category
    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
            .setTitle("Add Category")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val name = dialogBinding.editCategoryName.text.toString().trim()
                val icon = dialogBinding.editCategoryIcon.text.toString().trim()
                if (name.isNotEmpty()) {
                    // Create a new category for current user
                    val category = Category(userId = currentUserId, name = name, icon = icon)
                    lifecycleScope.launch {
                        repository.addCategory(category)  // Insert into DB
                        loadCategories()                  // Refresh list
                    }
                }
            }
            .setNegativeButton("Cancel", null)  // Dismiss dialog
            .show()
    }

    // Displays a dialog to edit an existing category
    private fun showEditCategoryDialog(category: Category) {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater).apply {
            // Pre-fill input fields with current category data
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
                    // Create an updated copy of the category with new values
                    val updated = category.copy(name = newName, icon = newIcon)
                    lifecycleScope.launch {
                        repository.updateCategory(updated)  // Update DB record
                        loadCategories()                    // Refresh list
                    }
                }
            }
            .setNegativeButton("Cancel", null)  // Dismiss dialog
            .show()
    }

    // Enables swipe-to-delete on RecyclerView items with undo support
    private fun attachSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            // onMove is not used but must be overridden
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            // Called when an item is swiped left or right
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val category = adapter.getItemAt(position)

                // Show confirmation dialog before deleting
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete '${category.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            repository.deleteCategory(category)  // Delete from DB
                            loadCategories()                    // Refresh list
                            // Show Snackbar with Undo option
                            Snackbar.make(binding.root, "Category deleted", Snackbar.LENGTH_LONG)
                                .setAction("Undo") {
                                    lifecycleScope.launch {
                                        repository.addCategory(category)  // Re-add deleted category
                                        loadCategories()
                                    }
                                }
                                .show()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        // Cancel deletion: notify adapter to rebind item and cancel swipe animation
                        adapter.notifyItemChanged(position)
                    }
                    .setCancelable(false)
                    .show()
            }
        })
        // Attach the ItemTouchHelper to the RecyclerView
        itemTouchHelper.attachToRecyclerView(binding.recyclerCategories)
    }

    // Clean up ViewBinding reference to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
