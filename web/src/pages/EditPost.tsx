import React from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import styled from '@emotion/styled';
import { useLocation } from 'react-router-dom';
import QuillEditor from '../components/CreatePost,EditPost/QuillEditor';
import PostSetting from '../components/CreatePost,EditPost/PostSetting';
import { useRecoilValue } from 'recoil';
import {
    titleState,
    descriptionState,
    imagesUrlListState,
    hashtagListState,
    categoryState,
    accessLevelState,
} from '../recoil/posts/atoms';

interface DataForEdit {
    savedTitle: string;
    savedDescription: string;
    savedHashtag: string[];
    savedCategory: string;
    savedAccessLevel: boolean;
    savedImages: string[];
}

const EditPost: React.FC = () => {
    const token = sessionStorage.getItem('token');

    const title = useRecoilValue(titleState);
    const description = useRecoilValue(descriptionState);
    const images = useRecoilValue(imagesUrlListState);
    const hashtagList = useRecoilValue(hashtagListState);
    const category = useRecoilValue(categoryState);
    const accessLevel = useRecoilValue(accessLevelState);

    const location = useLocation();
    const dataForEdit: DataForEdit = location.state.dataForEdit;
    const {
        savedTitle,
        savedDescription,
        savedImages,
        savedHashtag,
        savedCategory,
        savedAccessLevel,
    } = dataForEdit;

    const dataForQuillEditorComp = { savedTitle, savedDescription, savedImages };
    const dataForPostSettingComp = { savedHashtag, savedCategory, savedAccessLevel };

    const postForm = {
        title: title,
        description: description,
        images: images,
        hashtag: hashtagList,
        category: category,
        accessLevel: accessLevel,
    };
    console.log(postForm);

    const submitPostForm = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        try {
            console.log(token);
            const response = await axios.post('/api/posts', postForm, {
                headers: {
                    'X-AUTH-TOKEN': token,
                },
            });
            if (response.status === 200) {
                const navigate = useNavigate();
                navigate(`/posts/${response.data.postId}`);
            }
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <PostDataForm onSubmit={submitPostForm}>
            <QuillEditor {...dataForQuillEditorComp} />
            <PostSetting {...dataForPostSettingComp} />
            <SubmitButton type="submit">수정</SubmitButton>
        </PostDataForm>
    );
};

const PostDataForm = styled.form`
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    position: relative;
    margin: 150px auto;
    min-height: calc(100vh - 300px);
`;

const SubmitButton = styled.button`
    position: relative;
    left: 400px;
    padding: 3px 12px;
    border-radius: 12px;
    border-style: none;
`;

export default EditPost;
