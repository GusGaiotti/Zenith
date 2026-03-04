import api from "@/lib/api/axios";
import type {
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from "@/types/api";

export const getCategories = (ledgerId: number) => api.get<CategoryResponse[]>(`/ledgers/${ledgerId}/categories`);
export const createCategory = (ledgerId: number, body: CreateCategoryRequest) =>
  api.post<CategoryResponse>(`/ledgers/${ledgerId}/categories`, body);
export const updateCategory = (ledgerId: number, id: number, body: UpdateCategoryRequest) =>
  api.put<CategoryResponse>(`/ledgers/${ledgerId}/categories/${id}`, body);
export const deleteCategory = (ledgerId: number, id: number) =>
  api.delete<void>(`/ledgers/${ledgerId}/categories/${id}`);
