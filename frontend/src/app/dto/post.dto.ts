import { CommentDTO } from './comment.dto';

export interface PostDTO {
  id?: number;
  title: string;
  content: string;
  author?: string;
  comments: CommentDTO[];
}
