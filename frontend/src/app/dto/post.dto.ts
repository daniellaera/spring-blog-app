import {CommentDTO} from './comment.dto';

export interface PostDTO {
  id?: number;
  title: string;
  content: string;
  author?: string;
  createdAt?: string;
  updatedAt?: string;
  comments: CommentDTO[];
}
