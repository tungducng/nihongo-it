import api from '../utils/api';
import type { AxiosResponse } from 'axios';

export interface Category {
  categoryId: string;
  name: string;
  meaning: string;
  displayOrder: number;
  topicCount?: number;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCategoryRequest {
  name: string;
  meaning: string;
  description?: string;
  displayOrder?: number;
}

export interface UpdateCategoryRequest {
  name?: string;
  meaning?: string;
  description?: string;
  displayOrder?: number;
  isActive?: boolean;
}

export interface CategoryResponse {
  status: string;
  categories: Category[];
}

const categoryService = {
  // Public API endpoints
  getAllCategories(): Promise<AxiosResponse<Category[]>> {
    return api.get('/api/v1/categories');
  },

  getCategoryById(id: string): Promise<AxiosResponse<Category>> {
    return api.get(`/api/v1/categories/${id}`);
  },

  searchCategories(query: string): Promise<AxiosResponse<Category[]>> {
    return api.get(`/api/v1/categories/search?query=${encodeURIComponent(query)}`);
  },

  // Admin API endpoints
  adminGetAllCategories(): Promise<AxiosResponse<Category[]>> {
    return api.get('/api/admin/categories');
  },

  adminGetCategoryById(id: string): Promise<AxiosResponse<Category>> {
    return api.get(`/api/admin/categories/${id}`);
  },

  adminCreateCategory(data: CreateCategoryRequest): Promise<AxiosResponse<Category>> {
    return api.post('/api/admin/categories', data);
  },

  adminUpdateCategory(id: string, data: UpdateCategoryRequest): Promise<AxiosResponse<Category>> {
    return api.put(`/api/admin/categories/${id}`, data);
  },

  adminDeleteCategory(id: string): Promise<AxiosResponse<any>> {
    return api.delete(`/api/admin/categories/${id}`);
  },

  adminToggleCategoryStatus(id: string): Promise<AxiosResponse<Category>> {
    return api.patch(`/api/admin/categories/${id}/toggle-status`);
  },

  adminSearchCategories(nameQuery?: string, meaningQuery?: string): Promise<AxiosResponse<Category[]>> {
    let url = '/api/admin/categories/search';
    const params = new URLSearchParams();

    if (nameQuery) {
      params.append('query', nameQuery);
    }

    if (meaningQuery) {
      params.append('meaningQuery', meaningQuery);
    }

    const queryString = params.toString();
    return api.get(`${url}${queryString ? '?' + queryString : ''}`);
  }
};

export default categoryService;
