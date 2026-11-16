export interface Category {
  id: number;
  name: string;
}

export interface Tag {
  id: number;
  name: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  createdAt: string;
  category: Category | null;
  tags: Tag[];
}

export interface ProductPage {
  content: Product[];
  page: number;
  size: number;
  hasNext: boolean;
}
