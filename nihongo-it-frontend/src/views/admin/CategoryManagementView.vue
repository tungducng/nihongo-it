<template>
  <v-container fluid>
    <div class="d-flex align-center justify-space-between mb-4">
      <h1 class="text-h5">Quản lý danh mục</h1>
      <v-btn color="primary" @click="openDialog(null)" prepend-icon="mdi-plus">
        Thêm danh mục
      </v-btn>
    </div>

    <!-- Search and filter -->
    <v-card class="mb-4 pa-1" elevation="2">
      <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <v-text-field
              v-model="searchQuery.term"
              label="Tìm theo tên hoặc ý nghĩa"
              prepend-icon="mdi-magnify"
              hide-details
              density="compact"
              variant="outlined"
              @update:model-value="debounceSearch"
              placeholder="Nhập từ khóa để tìm kiếm (tên tiếng Nhật hoặc ý nghĩa tiếng Việt)"
            ></v-text-field>
          </v-col>
          <v-col cols="12" md="1" class="d-flex align-center">
            <v-btn
              variant="outlined"
              color="red"
              @click="clearSearch"
              class="ml-auto"
              size="small"
            >
              Xóa
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Loading indicator -->
    <div v-if="loading" class="d-flex justify-center my-5">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>

    <!-- Error message -->
    <v-alert v-if="error" type="error" class="mb-4">
      {{ error }}
    </v-alert>

    <!-- Categories table -->
    <v-card v-if="!loading && categories.length > 0" elevation="2" class="category-table-card">
      <v-table class="category-table">
        <thead>
          <tr>
            <th class="text-left">Tên (日本語)</th>
            <th class="text-left">Ý nghĩa (Tiếng Việt)</th>
            <th class="text-center">Thứ tự hiển thị</th>
            <th class="text-center">Số chủ đề</th>
            <th class="text-center">Ngày tạo</th>
            <th class="text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in categories" :key="item.categoryId" class="category-row">
            <td class="text-left">{{ item.name }}</td>
            <td class="text-left">{{ item.meaning }}</td>
            <td class="text-center">{{ item.displayOrder }}</td>
            <td class="text-center">
              <v-chip size="small" color="info" variant="outlined" class="topic-chip">
                {{ item.topicCount || 0 }}
              </v-chip>
            </td>
            <td class="text-center">{{ formatDate(item.createdAt) }}</td>
            <td class="text-center">
              <div class="actions-container">
                <v-tooltip text="Chỉnh sửa" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon
                      size="small"
                      variant="text"
                      color="primary"
                      @click="openDialog(item)"
                    >
                      <v-icon>mdi-pencil</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
                <v-tooltip text="Xóa danh mục" location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon
                      size="small"
                      variant="text"
                      color="error"
                      @click="confirmDelete(item)"
                    >
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
              </div>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>

    <v-card v-if="!loading && categories.length === 0" class="pa-4 text-center" elevation="2">
      <v-card-text>
        <div class="text-h6 mb-2">Không tìm thấy danh mục nào</div>
        <div v-if="searchQuery.term">Hãy thử điều chỉnh tiêu chí tìm kiếm</div>
        <div v-else>Bắt đầu bằng cách thêm danh mục mới</div>
      </v-card-text>
      <v-card-actions class="justify-center">
        <v-btn v-if="searchQuery.term" color="primary" variant="text" @click="clearSearch">
          Xóa tìm kiếm
        </v-btn>
        <v-btn v-else color="primary" @click="openDialog(null)" prepend-icon="mdi-plus">
          Thêm danh mục
        </v-btn>
      </v-card-actions>
    </v-card>

    <!-- Add/Edit dialog -->
    <v-dialog v-model="dialog" max-width="500px" persistent>
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          {{ formTitle }}
        </v-card-title>

        <v-card-text class="pt-4">
          <v-container>
            <v-form ref="form" v-model="valid">
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="editedItem.name"
                    label="Tên danh mục (日本語)"
                    prepend-icon="mdi-format-title"
                    :rules="[v => !!v || 'Tên danh mục không được để trống']"
                    required
                    autofocus
                    placeholder="Nhập tên bằng tiếng Nhật"
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="editedItem.meaning"
                    label="Ý nghĩa (Tiếng Việt)"
                    prepend-icon="mdi-translate"
                    :rules="[v => !!v || 'Ý nghĩa không được để trống']"
                    required
                    placeholder="Nhập ý nghĩa bằng tiếng Việt"
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model.number="editedItem.displayOrder"
                    label="Thứ tự hiển thị"
                    prepend-icon="mdi-sort-numeric-ascending"
                    type="number"
                    min="0"
                    hint="Số nhỏ hơn sẽ hiển thị trước"
                    persistent-hint
                  ></v-text-field>
                </v-col>
              </v-row>
            </v-form>
          </v-container>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="closeDialog">
            Hủy
          </v-btn>
          <v-btn color="primary" variant="elevated" @click="saveCategory" :disabled="!valid">
            Lưu
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete confirmation dialog -->
    <v-dialog v-model="deleteDialog" max-width="500px" persistent>
      <v-card>
        <v-card-title class="text-h5 bg-error text-white">Xóa danh mục</v-card-title>
        <v-card-text class="pt-4">
          <p class="text-body-1">Bạn có chắc chắn muốn xóa danh mục <strong>"{{ deleteItem?.name }}"</strong>?</p>
          <div class="mt-2 text-red-darken-2">Hành động này không thể hoàn tác.</div>
          <div v-if="deleteItem?.topicCount && deleteItem.topicCount > 0" class="mt-2 font-weight-bold bg-warning-lighten-5 pa-2 rounded">
            <v-icon color="warning" class="mr-1">mdi-alert</v-icon>
            Cảnh báo: Danh mục này chứa <strong>{{ deleteItem.topicCount }}</strong> chủ đề sẽ bị xóa theo.
          </div>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="deleteDialog = false">Hủy</v-btn>
          <v-btn color="error" variant="elevated" @click="deleteCategory">Xóa</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import categoryService from '@/services/categoryService';
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '@/services/categoryService';
import { useToast } from 'vue-toast-notification';
import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';

const $toast = useToast();

// Data
const categories = ref<Category[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const searchQuery = ref({
  term: ''
});
const valid = ref(false);
const form = ref(null);

// Dialog state
const dialog = ref(false);
const deleteDialog = ref(false);
const editedIndex = ref(-1);
const editedItem = ref<CreateCategoryRequest>({
  name: '',
  meaning: '',
  displayOrder: 0
});
const defaultItem = {
  name: '',
  meaning: '',
  displayOrder: 0
};
const deleteItem = ref<Category | null>(null);

// Format dates with Vietnamese locale
const formatDate = (dateString: string | undefined) => {
  if (!dateString) return 'N/A';
  try {
    return format(parseISO(dateString), 'dd/MM/yyyy', { locale: vi });
  } catch (e) {
    return 'Ngày không hợp lệ';
  }
};

// Computed properties
const formTitle = computed(() => {
  return editedIndex.value === -1 ? 'Thêm danh mục mới' : 'Chỉnh sửa danh mục';
});

// Methods
async function loadCategories() {
  loading.value = true;
  error.value = null;

  try {
    const response = await categoryService.adminGetAllCategories();
    categories.value = response.data;
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Không thể tải danh sách danh mục';
    console.error('Lỗi khi tải danh mục:', err);
  } finally {
    loading.value = false;
  }
}

function openDialog(item: Category | null) {
  if (item) {
    editedIndex.value = categories.value.findIndex(c => c.categoryId === item.categoryId);
    editedItem.value = {
      name: item.name,
      meaning: item.meaning,
      displayOrder: item.displayOrder
    };
  } else {
    editedIndex.value = -1;
    editedItem.value = { ...defaultItem };
  }
  dialog.value = true;
}

function closeDialog() {
  dialog.value = false;
  // Wait for dialog to close before resetting form
  setTimeout(() => {
    editedItem.value = { ...defaultItem };
    editedIndex.value = -1;
  }, 300);
}

function clearSearch() {
  searchQuery.value = {
    term: ''
  };
  loadCategories();
}

async function saveCategory() {
  if (!form.value) return;

  try {
    if (editedIndex.value > -1) {
      // Update existing category
      const categoryId = categories.value[editedIndex.value].categoryId;
      const updateData: UpdateCategoryRequest = {
        name: editedItem.value.name,
        meaning: editedItem.value.meaning,
        displayOrder: editedItem.value.displayOrder
      };

      await categoryService.adminUpdateCategory(categoryId, updateData);
      $toast.success('Cập nhật danh mục thành công');
    } else {
      // Create new category
      await categoryService.adminCreateCategory(editedItem.value);
      $toast.success('Thêm danh mục mới thành công');
    }

    closeDialog();
    loadCategories();
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Không thể lưu danh mục';
    $toast.error(error.value || 'Không thể lưu danh mục');
    console.error('Lỗi khi lưu danh mục:', err);
  }
}

function confirmDelete(item: Category) {
  deleteItem.value = item;
  deleteDialog.value = true;
}

async function deleteCategory() {
  if (!deleteItem.value) return;

  try {
    await categoryService.adminDeleteCategory(deleteItem.value.categoryId);
    $toast.success('Xóa danh mục thành công');
    deleteDialog.value = false;
    loadCategories();
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Không thể xóa danh mục';
    $toast.error(error.value || 'Không thể xóa danh mục');
    console.error('Lỗi khi xóa danh mục:', err);
  }
}

// Search with debounce
let debounceTimeout: number | null = null;
function debounceSearch() {
  if (debounceTimeout) clearTimeout(debounceTimeout);
  debounceTimeout = setTimeout(() => {
    searchCategories();
  }, 500) as unknown as number;
}

async function searchCategories() {
  loading.value = true;
  error.value = null;

  try {
    // Get search term
    let searchTerm = searchQuery.value.term.trim();

    if (!searchTerm) {
      await loadCategories();
      return;
    }

    // Use the same term for both name and meaning search
    const response = await categoryService.adminSearchCategories(searchTerm, searchTerm);
    categories.value = response.data;

  } catch (err: any) {
    error.value = err.response?.data?.message || 'Không thể tìm kiếm danh mục';
    console.error('Lỗi khi tìm kiếm danh mục:', err);
  } finally {
    loading.value = false;
  }
}

// Lifecycle hooks
onMounted(() => {
  loadCategories();
});
</script>

<style scoped>
.category-table-card {
  overflow: hidden;
  border-radius: 8px;
}

.category-table {
  width: 100%;
}

.category-table thead {
  background-color: rgba(var(--v-theme-primary), 0.05);
}

.category-table thead th {
  font-weight: 600;
  padding: 12px 16px;
}

.category-row:hover {
  background-color: rgba(var(--v-theme-primary), 0.03);
}

.category-row td {
  padding: 12px 16px;
  vertical-align: middle;
}

.actions-container {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.topic-chip {
  min-width: 40px;
}

.v-card-title {
  padding: 16px 20px;
}
</style>
